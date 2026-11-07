package com.ruoyi.library.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.library.domain.LibBook;
import com.ruoyi.library.mapper.LibBookMapper;
import com.ruoyi.library.mapper.LibCategoryMapper;
import com.ruoyi.library.service.ILibBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;

import java.util.List;
import java.util.Arrays;

/**
 * 图书信息 服务层处理
 */
@Service
public class LibBookServiceImpl implements ILibBookService {
    @Autowired
    private LibBookMapper libBookMapper;

    @Autowired
    private LibCategoryMapper libCategoryMapper;

    /**
     * 查询图书列表
     *
     * @param libBook 搜索条件
     * @return 图书集合
     */
    @Override
    public List<LibBook> selectLibBookList(LibBook libBook) {
        return libBookMapper.selectLibBookList(libBook);
    }

    @Override
    @Transactional
    public int insertLibBook(LibBook libBook) {
        checkCategoryForWrite(libBook.getCategoryId());
        libBook.setStockCount(libBook.getTotalCount());
        return libBookMapper.insertLibBook(libBook);
    }

    @Override
    public LibBook selectLibBookById(Long id) {
        return libBookMapper.selectLibBookById(id);
    }

    @Override
    @Transactional
    public int updateLibBook(LibBook libBook) {
        checkCategoryForWrite(libBook.getCategoryId());

        // 只有改了馆藏总数，才需要重新算可借数量
        if (libBook.getTotalCount() != null) {
            LibBook old = libBookMapper.selectLibBookByIdForUpdate(libBook.getBookId());
            if (old == null) {
                throw new ServiceException("图书不存在");
            }
            int borrowed = old.getTotalCount() - old.getStockCount();             // 已借出几本
            if (libBook.getTotalCount() < borrowed) {
                throw new ServiceException("馆藏总数不能少于已借出的 " + borrowed + " 本");
            }
            libBook.setStockCount(libBook.getTotalCount() - borrowed);            // 新的可借数量
        } else {
            libBook.setStockCount(null);    // 没改总数，就不允许单独改可借数量
        }
        return libBookMapper.updateLibBook(libBook);
    }

    // 和删除分类共用行锁，避免检查通过后分类被另一个请求删除。
    private void checkCategoryForWrite(Long categoryId) {
        if (categoryId != null && libCategoryMapper.selectLibCategoryByIdForUpdate(categoryId) == null) {
            throw new ServiceException("所选分类不存在");
        }
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public int deleteLibBookByIds(Long[] bookIds) {
        Long[] ids = Arrays.stream(bookIds).distinct().sorted().toArray(Long[]::new);
        for (Long bookId : ids) {
            LibBook book = libBookMapper.selectLibBookByIdForUpdate(bookId);
            if (book == null) {
                continue;                                  // 已经不存在的，跳过
            }
            int borrowed = book.getTotalCount() - book.getStockCount();
            if (borrowed > 0) {
                throw new ServiceException("《" + book.getBookName() + "》还有 " + borrowed + " 本未归还，不能删除");
            }
            if (libBookMapper.countBorrowHistory(bookId) > 0) {
                throw new ServiceException("《" + book.getBookName() + "》已有借阅记录，请下架以保留历史");
            }
        }
        return libBookMapper.deleteLibBookByIds(ids);
    }
}

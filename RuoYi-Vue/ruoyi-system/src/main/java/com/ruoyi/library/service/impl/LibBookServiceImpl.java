package com.ruoyi.library.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.library.domain.LibBook;
import com.ruoyi.library.mapper.LibBookMapper;
import com.ruoyi.library.service.ILibBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 图书信息 服务层处理
 */
@Service
public class LibBookServiceImpl implements ILibBookService {
    @Autowired
    private LibBookMapper libBookMapper;

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
    public int insertLibBook(LibBook libBook) {
        libBook.setStockCount(libBook.getTotalCount());
        return libBookMapper.insertLibBook(libBook);
    }

    @Override
    public LibBook selectLibBookById(Long id) {
        return libBookMapper.selectLibBookById(id);
    }

    @Override
    public int updateLibBook(LibBook libBook) {

        // 只有改了馆藏总数，才需要重新算可借数量
        if (libBook.getTotalCount() != null) {
            LibBook old = libBookMapper.selectLibBookById(libBook.getBookId());   // 先查出修改前的数据
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
}

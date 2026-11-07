package com.ruoyi.library.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.library.domain.LibBorrow;
import com.ruoyi.library.mapper.LibBookMapper;
import com.ruoyi.library.mapper.LibBorrowMapper;
import com.ruoyi.library.service.ILibBorrowService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LibBorrowServiceImpl implements ILibBorrowService {
    private final LibBorrowMapper borrowMapper;
    private final LibBookMapper bookMapper;

    public LibBorrowServiceImpl(LibBorrowMapper borrowMapper, LibBookMapper bookMapper) {
        this.borrowMapper = borrowMapper;
        this.bookMapper = bookMapper;
    }

    @Override
    public List<LibBorrow> selectMyBorrows(Long userId) {
        return borrowMapper.selectMyBorrows(userId);
    }

    @Override
    @Transactional
    public void borrowBook(Long bookId, Long userId, String username) {
        if (bookMapper.decreaseAvailableStock(bookId) != 1) {
            throw new ServiceException("图书不存在、已下架或已借完");
        }
        if (borrowMapper.countUnreturned(bookId, userId) > 0) {
            throw new ServiceException("你已借阅这本书，请归还后再借");
        }
        LibBorrow borrow = new LibBorrow();
        borrow.setUserId(userId);
        borrow.setBookId(bookId);
        borrow.setCreateBy(username);
        if (borrowMapper.insertBorrow(borrow) != 1) {
            throw new ServiceException("借阅记录保存失败");
        }
    }

    @Override
    @Transactional
    public void returnBook(Long borrowId, Long userId, String username) {
        Long bookId = borrowMapper.selectMyBookId(borrowId, userId);
        if (bookId == null) {
            throw new ServiceException("未找到你的借阅记录");
        }
        if (borrowMapper.markReturned(borrowId, userId, username) != 1) {
            throw new ServiceException("该借阅记录已归还或状态异常");
        }
        if (bookMapper.restoreAvailableStock(bookId) != 1) {
            throw new ServiceException("图书库存异常，未能归还");
        }
    }
}

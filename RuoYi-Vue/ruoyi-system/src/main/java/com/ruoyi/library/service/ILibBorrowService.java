package com.ruoyi.library.service;

import com.ruoyi.library.domain.LibBorrow;
import java.util.List;

public interface ILibBorrowService {
    List<LibBorrow> selectMyBorrows(Long userId);

    void borrowBook(Long bookId, Long userId, String username);

    void returnBook(Long borrowId, Long userId, String username);
}

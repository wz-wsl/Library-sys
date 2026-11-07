package com.ruoyi.library.mapper;

import com.ruoyi.library.domain.LibBorrow;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface LibBorrowMapper {
    List<LibBorrow> selectMyBorrows(Long userId);

    int insertBorrow(LibBorrow borrow);

    int countUnreturned(@Param("bookId") Long bookId, @Param("userId") Long userId);

    Long selectMyBookId(@Param("borrowId") Long borrowId, @Param("userId") Long userId);

    int markReturned(@Param("borrowId") Long borrowId, @Param("userId") Long userId,
                     @Param("username") String username);
}

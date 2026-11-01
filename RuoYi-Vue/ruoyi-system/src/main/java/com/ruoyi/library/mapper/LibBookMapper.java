package com.ruoyi.library.mapper;

import com.ruoyi.library.domain.LibBook;

import java.util.List;

/**
 * 图书信息 数据层
 */
public interface LibBookMapper {
    /**
     * 查询图书列表
     *
     * @param libBook 搜索条件
     * @return 图书集合
     */
    List<LibBook> selectLibBookList(LibBook libBook);
    int insertLibBook(LibBook libBook);
    LibBook selectLibBookById(Long bookId);
    int updateLibBook(LibBook libBook);
    int deleteLibBookByIds(Long[] bookId);
}

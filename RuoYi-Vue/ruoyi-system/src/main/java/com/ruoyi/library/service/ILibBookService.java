package com.ruoyi.library.service;

import com.ruoyi.library.domain.LibBook;

import java.util.List;

/**
 * 图书信息 服务层
 */
public interface ILibBookService {
    /**
     * 查询图书列表
     *
     * @param libBook 搜索条件
     * @return 图书集合
     */
    List<LibBook> selectLibBookList(LibBook libBook);
    int insertLibBook(LibBook libBook);
    LibBook selectLibBookById(Long id);
    int updateLibBook(LibBook libBook);
}

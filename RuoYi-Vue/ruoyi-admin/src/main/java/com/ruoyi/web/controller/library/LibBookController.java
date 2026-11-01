package com.ruoyi.web.controller.library;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.library.domain.LibBook;
import com.ruoyi.library.service.ILibBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/library/book")
public class LibBookController extends BaseController {

    @Autowired
    private ILibBookService libBookService;

    @GetMapping("/list")
    @PreAuthorize("@ss.hasPermi('library:book:list')")
    public TableDataInfo list(LibBook libBook) {           // ② 返回 TableDataInfo
        startPage();                                       // ③ 开启分页
        List<LibBook> list = libBookService.selectLibBookList(libBook);
        return getDataTable(list);                         // ④ 包装
    }

    @PostMapping("/add")
    @PreAuthorize("@ss.hasPermi('library:book:add')")
    @Log(title = "图书信息",businessType = BusinessType.INSERT)
    public AjaxResult add(@Validated @RequestBody LibBook libBook) {
        libBook.setCreateBy(getUsername());
        return toAjax(libBookService.insertLibBook(libBook));
    }

    @PreAuthorize("@ss.hasPermi('library:book:query')")
    @GetMapping("/{bookId}")
    public AjaxResult getInfo(@PathVariable("bookId") Long bookId) {
        return success(libBookService.selectLibBookById(bookId));
    }

    @PreAuthorize("@ss.hasPermi('library:book:edit')")
    @Log(title = "图书信息", businessType = BusinessType.UPDATE)
    @PutMapping("/edit")
    public AjaxResult edit(@Validated @RequestBody LibBook libBook) {
        libBook.setUpdateBy(getUsername());
        return toAjax(libBookService.updateLibBook(libBook));
    }
}

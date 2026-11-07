package com.ruoyi.web.controller.library;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.library.service.ILibBorrowService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/library/borrow")
public class LibBorrowController extends BaseController {
    private final ILibBorrowService borrowService;

    public LibBorrowController(ILibBorrowService borrowService) {
        this.borrowService = borrowService;
    }

    @GetMapping("/list")
    @PreAuthorize("@ss.hasPermi('library:borrow:list')")
    public TableDataInfo list() {
        Long userId = getUserId();
        startPage();
        return getDataTable(borrowService.selectMyBorrows(userId));
    }

    @PostMapping("/{bookId}")
    @PreAuthorize("@ss.hasPermi('library:borrow:add')")
    @Log(title = "图书借阅", businessType = BusinessType.INSERT)
    public AjaxResult borrow(@PathVariable("bookId") Long bookId) {
        borrowService.borrowBook(bookId, getUserId(), getUsername());
        return success("借书成功");
    }

    @PutMapping("/return/{borrowId}")
    @PreAuthorize("@ss.hasPermi('library:borrow:return')")
    @Log(title = "图书归还", businessType = BusinessType.UPDATE)
    public AjaxResult returnBook(@PathVariable("borrowId") Long borrowId) {
        borrowService.returnBook(borrowId, getUserId(), getUsername());
        return success("还书成功");
    }
}

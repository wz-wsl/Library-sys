import request from '@/utils/request'

export function listMyBorrows(params) {
  return request({ url: '/library/borrow/list', method: 'get', params })
}

export function borrowBook(bookId) {
  return request({ url: '/library/borrow/' + bookId, method: 'post' })
}

export function returnBook(borrowId) {
  return request({ url: '/library/borrow/return/' + borrowId, method: 'put' })
}

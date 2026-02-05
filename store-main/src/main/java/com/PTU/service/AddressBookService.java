package com.PTU.service;

import com.PTU.entity.AddressBook;
import com.baomidou.mybatisplus.extension.service.IService;

public interface AddressBookService extends IService<AddressBook> {
    void setDefault(AddressBook addressBook);
}

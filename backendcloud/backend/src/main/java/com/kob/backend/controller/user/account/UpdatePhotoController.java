package com.kob.backend.controller.user.account;

import com.kob.backend.service.user.account.UpdatePhotoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class UpdatePhotoController {
    @Autowired
    private UpdatePhotoService updatePhotoService;

    @PostMapping("/api/user/account/update/")
    public Map<String, String> update(@RequestParam Map<String, String> map) {
        String photo = map.get("photo");
        return updatePhotoService.update(photo);
    }
}

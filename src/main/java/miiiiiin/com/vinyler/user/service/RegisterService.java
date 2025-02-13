package miiiiiin.com.vinyler.user.service;

import miiiiiin.com.vinyler.user.dto.ServiceRegisterDto;
import miiiiiin.com.vinyler.user.entity.User;

public interface RegisterService {
    User registerUser(ServiceRegisterDto dto);
}

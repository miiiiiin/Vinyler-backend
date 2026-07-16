package miiiiiin.com.vinyler.application.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import miiiiiin.com.vinyler.application.dto.UserVinylStatusDto;
import miiiiiin.com.vinyler.application.dto.request.LikeRequestDto;
import miiiiiin.com.vinyler.application.entity.UserVinylStatus;
import miiiiiin.com.vinyler.application.repository.UserVinylStatusRepository;
import miiiiiin.com.vinyler.user.entity.User;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserVinylServiceStatusImpl implements UserVinylStatusService {

    private final VinylService vinylService;
    private final UserVinylStatusRepository userVinylStatusRepository;

    @Override
    @Transactional
    public UserVinylStatusDto toggleListenStatus(LikeRequestDto requestDto, User currentUser) {
        var vinylEntity = vinylService.findOrCreateVinyl(requestDto, currentUser);

        var status = userVinylStatusRepository.findByUserAndVinyl(currentUser, vinylEntity);

        if (status.isPresent() && status.get().isListened()) {
            userVinylStatusRepository.delete(status.get());
            return UserVinylStatusDto.from(vinylEntity, currentUser, false);
        } else {
            userVinylStatusRepository.save(UserVinylStatus.of(currentUser, vinylEntity, true));
            return UserVinylStatusDto.from(vinylEntity, currentUser, true);
        }
    }
}

package miiiiiin.com.vinyler.application.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import miiiiiin.com.vinyler.application.dto.UserVinylStatusDto;
import miiiiiin.com.vinyler.application.dto.request.LikeRequestDto;
import miiiiiin.com.vinyler.application.entity.UserVinylStatus;
import miiiiiin.com.vinyler.application.entity.vinyl.Vinyl;
import miiiiiin.com.vinyler.application.repository.UserVinylStatusRepository;
import miiiiiin.com.vinyler.application.repository.VinylRepository;
import miiiiiin.com.vinyler.user.entity.User;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserVinylServiceStatusImpl implements UserVinylStatusService {

    private final VinylRepository vinylRepository;
    private final UserVinylStatusRepository userVinylStatusRepository;


    // 사용자가 Vinyl의 감상 상태를 토글 (감상 완료/감상 미완료)
    @Override
    @Transactional
    public UserVinylStatusDto toggleListenStatus(LikeRequestDto requestDto, User currentUser) {
        // Vinyl 엔티티가 DB에 존재하는지 확인, 없으면 저장
        var entity = Vinyl.of(requestDto, currentUser);

        var vinylEntity = vinylRepository.findByDiscogsId(entity.getDiscogsId())
                .orElseGet(() -> {
                    // 새로 저장할 때는 vinylId를 설정할 필요 없음 (자동 생성됨)
                    return vinylRepository.save(entity);
                });

        var status = userVinylStatusRepository.findByUserAndVinyl(currentUser, vinylEntity);

        if (status.isPresent() && status.get().isListened()) {
            userVinylStatusRepository.delete(status.get());
            return UserVinylStatusDto.from(vinylRepository.save(vinylEntity), currentUser, false);
        } else {
            var st = UserVinylStatus.of(currentUser, vinylEntity, true);
            userVinylStatusRepository.save(st);
            return UserVinylStatusDto.from(vinylRepository.save(vinylEntity), currentUser, true);
        }
    }
}

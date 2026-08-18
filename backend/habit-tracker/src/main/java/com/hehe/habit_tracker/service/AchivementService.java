package com.hehe.habit_tracker.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.hehe.habit_tracker.dto.request.AchivementCreationRequest;
import com.hehe.habit_tracker.dto.request.AchivementUpdateRequest;
import com.hehe.habit_tracker.dto.response.AchivementResponse;
import com.hehe.habit_tracker.entity.Achivement;
import com.hehe.habit_tracker.exception.AppException;
import com.hehe.habit_tracker.exception.ErrorCode;
import com.hehe.habit_tracker.mapper.AchivementMapper;
import com.hehe.habit_tracker.repository.AchivementRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * CRUD cho catalog định nghĩa thành tựu.
 * Chưa bao gồm logic đánh giá/cấp thành tựu (unlock engine) — làm sau.
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AchivementService {

    AchivementRepository achivementRepository;
    AchivementMapper achivementMapper;

    /** Bước nhảy giữa các vị trí — chừa khoảng trống để chèn/sắp xếp lại về sau. */
    private static final int SORT_ORDER_STEP = 10;

    public AchivementResponse createAchivement(AchivementCreationRequest request) {
        if (achivementRepository.existsByCode(request.code())) {
            throw new AppException(ErrorCode.ACHIEVEMENT_CODE_EXISTED);
        }
        Achivement achivement = achivementMapper.toAchivement(request);
        // Tự đặt vị trí kế tiếp: cái mới luôn xuống cuối danh sách.
        int nextSortOrder = achivementRepository.findFirstByOrderBySortOrderDesc()
                .map(last -> last.getSortOrder() + SORT_ORDER_STEP)
                .orElse(SORT_ORDER_STEP);
        achivement.setSortOrder(nextSortOrder);
        return achivementMapper.toAchivementResponse(achivementRepository.save(achivement));
    }

    /** Trả về theo sortOrder tăng dần để UI hiển thị đúng thứ tự. */
    public List<AchivementResponse> getAllAchivements() {
        return achivementRepository.findAllByOrderBySortOrderAsc()
                .stream()
                .map(achivementMapper::toAchivementResponse)
                .toList();
    }

    public AchivementResponse getAchivementById(Long id) {
        Achivement achivement = achivementRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ACHIEVEMENT_NOT_FOUND));
        return achivementMapper.toAchivementResponse(achivement);
    }

    public AchivementResponse updateAchivement(Long id, AchivementUpdateRequest request) {
        Achivement achivement = achivementRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ACHIEVEMENT_NOT_FOUND));
        achivementMapper.updateAchivement(achivement, request);
        return achivementMapper.toAchivementResponse(achivementRepository.save(achivement));
    }

    public void deleteAchivement(Long id) {
        if (!achivementRepository.existsById(id)) {
            throw new AppException(ErrorCode.ACHIEVEMENT_NOT_FOUND);
        }
        achivementRepository.deleteById(id);
    }
}

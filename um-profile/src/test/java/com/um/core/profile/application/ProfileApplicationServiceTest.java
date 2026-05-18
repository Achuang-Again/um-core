package com.um.core.profile.application;

import com.um.core.common.exception.BusinessException;
import com.um.core.infrastructure.persistence.mapper.SysUserProfileMapper;
import com.um.core.infrastructure.persistence.po.SysUserProfilePO;
import com.um.core.profile.application.command.UpdateProfileCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileApplicationServiceTest {

    @Mock
    private SysUserProfileMapper profileMapper;

    @InjectMocks
    private ProfileApplicationService profileApplicationService;

    @Test
    @DisplayName("场景：更新昵称与扩展字段")
    void updateProfile_shouldMergeExtPatch() {
        SysUserProfilePO po = new SysUserProfilePO();
        po.setUserId(1L);
        po.setNickname("old");
        po.setExtData("{}");
        when(profileMapper.selectById(1L)).thenReturn(po);

        var cmd = new UpdateProfileCommand(1L, "newName", null, null, null,
                Map.of("signature", "hello"));
        SysUserProfilePO updated = profileApplicationService.updateProfile(cmd);

        assertThat(updated.getNickname()).isEqualTo("newName");
        assertThat(updated.getExtData()).contains("signature");
        verify(profileMapper).updateById(any());
    }

    @Test
    @DisplayName("场景：资料不存在，抛出异常")
    void getProfile_shouldThrow_whenNotFound() {
        when(profileMapper.selectById(99L)).thenReturn(null);
        assertThatThrownBy(() -> profileApplicationService.getProfile(99L))
                .isInstanceOf(BusinessException.class);
    }
}

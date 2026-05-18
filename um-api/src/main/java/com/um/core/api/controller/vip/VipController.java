package com.um.core.api.controller.vip;

import com.um.core.api.dto.vip.VipPurchaseRequest;
import com.um.core.api.dto.vip.VipStatusResponse;
import com.um.core.api.dto.vip.VipUpgradeRequest;
import com.um.core.api.security.UserPrincipal;
import com.um.core.common.web.ApiResponse;
import com.um.core.vip.application.VipApplicationService;
import com.um.core.vip.application.command.VipSubscribeCommand;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/um/vip")
@RequiredArgsConstructor
@Tag(name = "VIP会员")
public class VipController {

    private final VipApplicationService vipApplicationService;

    @GetMapping("/me")
    @Operation(summary = "查询当前 VIP 状态")
    public ApiResponse<VipStatusResponse> me() {
        return ApiResponse.ok(VipStatusResponse.from(
                vipApplicationService.getMyVip(UserPrincipal.currentUserId())));
    }

    @PostMapping("/subscribe")
    @Operation(summary = "开通 VIP")
    public ApiResponse<VipStatusResponse> subscribe(
            @Valid @RequestBody VipPurchaseRequest req,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        var cmd = new VipSubscribeCommand(UserPrincipal.currentUserId(), req.levelCode(),
                req.durationDays(), req.orderNo(), req.paidAmount(),
                req.idempotencyKey() != null ? req.idempotencyKey() : idempotencyKey);
        return ApiResponse.ok(VipStatusResponse.from(vipApplicationService.subscribe(cmd)));
    }

    @PostMapping("/renew")
    @Operation(summary = "续费 VIP")
    public ApiResponse<VipStatusResponse> renew(
            @Valid @RequestBody VipPurchaseRequest req,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        var cmd = new VipSubscribeCommand(UserPrincipal.currentUserId(), req.levelCode(),
                req.durationDays(), req.orderNo(), req.paidAmount(),
                req.idempotencyKey() != null ? req.idempotencyKey() : idempotencyKey);
        return ApiResponse.ok(VipStatusResponse.from(vipApplicationService.renew(cmd)));
    }

    @PostMapping("/upgrade")
    @Operation(summary = "升级 VIP")
    public ApiResponse<VipStatusResponse> upgrade(
            @Valid @RequestBody VipUpgradeRequest req,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        return ApiResponse.ok(VipStatusResponse.from(
                vipApplicationService.upgrade(UserPrincipal.currentUserId(), req.newLevelCode(),
                        req.purchasedDays(), req.orderNo(), req.paidAmount(),
                        req.idempotencyKey() != null ? req.idempotencyKey() : idempotencyKey)));
    }
}

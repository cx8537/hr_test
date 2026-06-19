package com.example.hr.approval.domain;

/** 단계 내 결재자 1인의 상태(스냅샷의 원 결재자 식별자는 영속 계층에서 보존). */
public record StepMember(MemberState state) {
}

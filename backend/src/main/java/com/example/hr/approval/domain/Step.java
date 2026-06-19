package com.example.hr.approval.domain;

import java.util.List;

/** 결재선의 한 단계(유형 + 결재자들). 합의/병렬은 전원 처리 기준(AP-011/013). */
public record Step(StepType type, List<StepMember> members) {
}

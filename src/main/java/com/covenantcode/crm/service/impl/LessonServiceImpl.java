package com.covenantcode.crm.service.impl;


import com.covenantcode.crm.dto.lesson.LessonCreateRequest;
import com.covenantcode.crm.dto.lesson.LessonResponse;
import com.covenantcode.crm.entity.Lesson;

import com.covenantcode.crm.entity.User;

import com.covenantcode.crm.exception.ForbiddenException;
import com.covenantcode.crm.exception.ResourceNotFoundException;
import com.covenantcode.crm.mapper.LessonMapper;
import com.covenantcode.crm.repository.LessonRepository;
import com.covenantcode.crm.repository.LessonSpecification;
import com.covenantcode.crm.repository.UserRepository;
import com.covenantcode.crm.service.LessonService;

import com.covenantcode.crm.utils.CurrentUserProvider;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@AllArgsConstructor
public class LessonServiceImpl implements LessonService {

    private final LessonRepository lessonRepository;
    private final UserRepository userRepository;
    private final LessonMapper lessonMapper;

    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional(readOnly = true)
    public Page<LessonResponse> getAll(Pageable pageable) {
        Long currentUserId = currentUserProvider.getCurrentUserId();

        Specification<Lesson> spec = Specification.where(null);

        if (currentUserProvider.isTeacher()) {
            spec = spec.and(LessonSpecification.hasTeacherId(currentUserId));
        }

        return lessonRepository.findAll(spec, pageable)
                .map(lessonMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public LessonResponse getById(Long id) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found"));

        if (currentUserProvider.isTeacher()) {
            checkTeacherHasAccessToLesson(lesson);
        }

        return lessonMapper.toResponse(lesson);
    }

    @Override
    @Transactional
    public LessonResponse create(LessonCreateRequest request) {
        User teacher = userRepository.findById(request.getTeacherId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getTeacherId()));

        Lesson lesson = lessonMapper.toEntity(request, teacher);

        Lesson savedLesson = lessonRepository.save(lesson);

        return lessonMapper.toResponse(savedLesson);
    }

    @Override
    @Transactional
    public LessonResponse update(Long id, LessonCreateRequest request) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", id));

        User teacher = userRepository.findById(request.getTeacherId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getTeacherId()));

        lessonMapper.updateEntity(lesson, request, teacher);

        Lesson savedLesson = lessonRepository.save(lesson);

        return lessonMapper.toResponse(savedLesson);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!lessonRepository.existsById(id)) {
            throw new ResourceNotFoundException("Lesson", id);
        }

        lessonRepository.deleteById(id);
    }

    private void checkTeacherHasAccessToLesson(Lesson lesson) {
        Long currentUserId = currentUserProvider.getCurrentUserId();

        if (lesson.getTeacher() == null || !lesson.getTeacher().getId().equals(currentUserId)) {
            throw new ForbiddenException("У вас нет доступа к этому занятию");
        }
    }
}

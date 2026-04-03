package com.evggenn.edugo.subject;

import com.evggenn.edugo.exception.SubjectAlreadyExistsException;
import com.evggenn.edugo.exception.SubjectNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubjectService {

    private final SubjectRepository subjectRepository;

    @Transactional
    public Subject createSubject(String name) {
        if (subjectRepository.existsByNameIgnoreCase(name)) {
            throw new SubjectAlreadyExistsException(name);
        }

        Subject subject = new Subject(name);

        return subjectRepository.save(subject);
    }

    @Transactional
    public Subject updateSubject(Long id, String newName) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new SubjectNotFoundException(id));

        if (subjectRepository.existsByNameIgnoreCase(newName) &&
                (!subject.getName().equalsIgnoreCase(newName))) {
            throw new SubjectAlreadyExistsException(newName);
        }

        subject.setName(newName);

        return subjectRepository.save(subject);
    }

    @Transactional(readOnly = true)
    public Subject getSubject(Long id) {
        return subjectRepository.findById(id)
                .orElseThrow(() -> new SubjectNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<Subject> getAllSubjects() {
        return subjectRepository.findAll();
    }
}

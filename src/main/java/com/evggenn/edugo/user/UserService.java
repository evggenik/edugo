package com.evggenn.edugo.user;

import com.evggenn.edugo.subject.Subject;
import com.evggenn.edugo.subject.SubjectRepository;
import com.evggenn.edugo.subject.exception.SubjectNotFoundException;
import com.evggenn.edugo.user.exception.*;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final SubjectRepository subjectRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public User findByEmailWithRolesOrThrow(@NotNull String email) {
        return userRepo.findByEmailWithRoles(email)
                .orElseThrow(() -> new UserNotFoundException(email));
    }

    @Transactional
    public User createUser(String email,
                           String rawPassword,
                           String firstName,
                           String lastName,
                           String middleName,
                           RoleName roleName) {

        if (userRepo.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }

        String encodedPassword = passwordEncoder.encode(rawPassword);

        Role role = roleRepo.findByName(roleName)
                .orElseThrow(() -> new SchoolRoleNotFoundException(roleName));

        User user = User.builder()
                .email(email)
                .password(encodedPassword)
                .firstName(firstName)
                .lastName(lastName)
                .middleName(middleName)
                .build();

        user.addRole(role);

        return userRepo.save(user);
    }

    @Transactional
    public void addSubjectToTeacher(Long teacherId,Long subjectId) {

        User teacher = findTeacherByIdOrThrow(teacherId);

        Subject subject = subjectRepository.findById(subjectId).orElseThrow(
                () -> new SubjectNotFoundException(subjectId)
        );

        if (teacher.getSubjects().stream()
                .anyMatch(subj -> subj.getId().equals(subjectId))) {
            throw new TeacherAlreadyHasSubjectException(subject.getName(), teacher.getLastName());
        }

        teacher.getSubjects().add(subject);
    }

    @Transactional
    public void removeSubjectFromTeacher(Long teacherId, Long subjectId) {

        User teacher = findTeacherByIdOrThrow(teacherId);

        Subject subject = subjectRepository.findById(subjectId).orElseThrow(
                () -> new SubjectNotFoundException(subjectId)
        );

        if (teacher.getSubjects().stream()
                .noneMatch(subj -> subj.getId().equals(subjectId))) {
            throw new TeacherHasNotSubjectException(teacher.getLastName(), subject.getName());
        }

        teacher.getSubjects()
                .removeIf(subj -> subj.getId().equals(subjectId));
    }

    public void assignRole(Long userId, Long roleId) {

    }

    public User findTeacherByIdOrThrow(Long teacherId) {
        User teacher = userRepo.findByIdWithRoles(teacherId)
                .orElseThrow(() -> new UserNotFoundException(teacherId));
        if (teacher.getRoles().stream()
                .noneMatch(role -> role.getName() == RoleName.TEACHER)) {
            throw new NotTeacherException(teacherId);
        }
        return teacher;
    }
}

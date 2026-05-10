package com.evggenn.edugo.user;

import com.evggenn.edugo.subject.Subject;
import com.evggenn.edugo.subject.SubjectRepository;
import com.evggenn.edugo.subject.exception.SubjectNotFoundException;
import com.evggenn.edugo.user.exception.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private Role studentRole;

    @Mock
    private UserRepository userRepo;

    @Mock
    private RoleRepository roleRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SubjectRepository subjectRepo;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        studentRole = Role.builder()
                .id(1L)
                .name(RoleName.STUDENT)
                .build();
    }

    @Test
    public void createUser_shouldReturnUser_whenEmailNotExists() {
        User student = buildStudent();

        when(userRepo.existsByEmail("test@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("hash_password");
        when(roleRepo.findByName(RoleName.STUDENT)).thenReturn(Optional.of(studentRole));
        when(userRepo.save(any(User.class))).thenReturn(student);

        User createdUser = userService.createUser(
                "test@test.com",
                "password",
                "Vasia",
                "Vasilkov",
                "Zagagulivich",
                RoleName.STUDENT
        );

        assertThat(createdUser.getId()).isEqualTo(student.getId());
        assertThat(createdUser.getEmail()).isEqualTo("test@test.com");
        assertThat(createdUser.getFirstName()).isEqualTo("Vasia");
        assertThat(createdUser.getPassword()).isEqualTo("hash_password");

        verify(userRepo).existsByEmail("test@test.com");
        verify(passwordEncoder).encode("password");
        verify(roleRepo).findByName(RoleName.STUDENT);
        verify(userRepo).save(any(User.class));
    }
    @Test
    public void createUser_whenEmailExists_throwsEmailAlreadyExistsException() {

        when(userRepo.existsByEmail("test@test.com")).thenReturn(true);

        Throwable thrown = assertThrows(EmailAlreadyExistsException.class, () -> {
            userService.createUser(
                    "test@test.com",
                    "password",
                    "Vasia",
                    "Vasilkov",
                    "Zagagulivich",
                    RoleName.STUDENT
            );
        });

        assertEquals("Email already exists: test@test.com", thrown.getMessage());
    }

    @Test
    public void createUser_whenRoleNotExists_throwsSchoolRoleNotFoundException() {

        when(userRepo.existsByEmail("test@test.com")).thenReturn(false);
        when(roleRepo.findByName(RoleName.STUDENT)).thenReturn(Optional.empty());

        Throwable thrown = assertThrows(SchoolRoleNotFoundException.class, () -> {
            userService.createUser(
                    "test@test.com",
                    "password",
                    "Vasia",
                    "Vasilkov",
                    "Zagagulivich",
                    RoleName.STUDENT
            );
        });

        assertEquals("SchoolRole not found: " + RoleName.STUDENT, thrown.getMessage());
    }

    @Test
    public void findByEmailWithRolesOrThrow_whenUserFound_returnsUser() {
        User student = buildStudent();

        when(userRepo.findByEmailWithRoles(student.getEmail())).thenReturn(Optional.of(student));

        User createdUser = userService.findByEmailWithRolesOrThrow("test@test.com");

        assertThat(createdUser.getRoles()).hasSize(1);
        assertThat(createdUser.getRoles()).extracting(Role::getName).containsExactlyInAnyOrder(RoleName.STUDENT);

    }

    @Test
    public void findByEmailWithRolesOrThrow_whenUserNotFound_throwsException() {
        User student = buildStudent();

        when(userRepo.findByEmailWithRoles(student.getEmail())).thenReturn(Optional.empty());

        Throwable thrown = assertThrows(UserNotFoundException.class, () -> {
            userService.findByEmailWithRolesOrThrow(student.getEmail());
        });

        assertEquals("User not found with email: " + student.getEmail(), thrown.getMessage());
    }

    @Test
    public void addSubjectToTeacher_shouldAddSubject_whenTeacherExists() {
        User teacher = buildTeacher();
        Subject subject = new Subject("Математика");
        subject.setId(1L);

        when(userRepo.findByIdWithRoles(teacher.getId()))
                .thenReturn(Optional.of(teacher));
        when(subjectRepo.findById(1L)).thenReturn(Optional.of(subject));

        userService.addSubjectToTeacher(teacher.getId(), subject.getId());

        assertThat(teacher.getSubjects()).hasSize(1);
        assertThat(teacher.getSubjects()).contains(subject);

        verify(userRepo).findByIdWithRoles(teacher.getId());
        verify(subjectRepo).findById(1L);
        verify(userRepo, never()).save(any());
    }

    @Test
    public void addSubjectToTeacher_shouldThrow_whenUserNotFound() {
        when(userRepo.findByIdWithRoles(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> userService.addSubjectToTeacher(1L, 1L));

        verify(subjectRepo, never()).findById(any());
    }

    @Test
    public void addSubjectToTeacher_shouldThrow_whenUserNotTeacher() {
        User student = buildStudent();

        when(userRepo.findByIdWithRoles(1L)).thenReturn(Optional.of(student));

        assertThrows(NotTeacherException.class,
                () -> userService.addSubjectToTeacher(1L, 1L));

        verify(subjectRepo, never()).findById(any());
    }

    @Test
    public void addSubjectToTeacher_shouldThrow_whenSubjectNotFound() {
        User teacher = buildTeacher();

        when(userRepo.findByIdWithRoles(1L)).thenReturn(Optional.of(teacher));
        when(subjectRepo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(SubjectNotFoundException.class,
                () -> userService.addSubjectToTeacher(1L, 1L));

        verify(subjectRepo).findById(1L);
        verifyNoMoreInteractions(subjectRepo);
        assertThat(teacher.getSubjects()).isEmpty();
    }

    @Test
    public void addSubjectToTeacher_shouldThrow_whenDuplicateSubject() {
        User teacher = buildTeacher();
        Subject subject = new Subject("Математика");
        subject.setId(1L);
        teacher.getSubjects().add(subject);

        when(userRepo.findByIdWithRoles(1L)).thenReturn(Optional.of(teacher));
        when(subjectRepo.findById(1L)).thenReturn(Optional.of(subject));

        assertThrows(TeacherAlreadyHasSubjectException.class,
                () -> userService.addSubjectToTeacher(1L, 1L));

        assertThat(teacher.getSubjects())
                .containsExactly(subject);
    }

    @Test
    public void removeSubjectFromTeacher_shouldRemoveSubject() {
        User teacher = buildTeacher();
        Subject math = new Subject("Математика");
        math.setId(1L);
        teacher.getSubjects().add(math);
        Subject physics = new Subject("Физика");
        physics.setId(2L);
        teacher.getSubjects().add(physics);

        when(userRepo.findByIdWithRoles(1L)).thenReturn(Optional.of(teacher));
        when(subjectRepo.findById(1L)).thenReturn(Optional.of(math));

        userService.removeSubjectFromTeacher(1L, 1L);

        assertThat(teacher.getSubjects()).containsExactly(physics);
    }

    @Test
    public void removeSubjectFromTeacher_shouldThrow_whenUserNotFound() {
        when(userRepo.findByIdWithRoles(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> userService.removeSubjectFromTeacher(1L, 1L));

        verify(subjectRepo, never()).findById(any());
    }

    @Test
    public void removeSubjectFromTeacher_shouldThrow_whenUserNotTeacher() {
        User student = buildStudent();

        when(userRepo.findByIdWithRoles(1L)).thenReturn(Optional.of(student));

        assertThrows(NotTeacherException.class,
                () -> userService.removeSubjectFromTeacher(1L, 1L));

        verify(subjectRepo, never()).findById(any());
    }

    @Test
    public void removeSubjectFromTeacher_shouldThrow_whenSubjectNotFound() {
        User teacher = buildTeacher();
        Subject maths = new Subject("Математика");
        maths.setId(1L);
        teacher.getSubjects().add(maths);

        when(userRepo.findByIdWithRoles(1L)).thenReturn(Optional.of(teacher));
        when(subjectRepo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(SubjectNotFoundException.class,
                () -> userService.removeSubjectFromTeacher(1L, 1L));

        assertThat(teacher.getSubjects()).containsExactly(maths);
        verify(subjectRepo).findById(1L);
        verifyNoMoreInteractions(subjectRepo);
    }

    @Test
    public void removeSubjectFromTeacher_shouldThrow_whenTeacherHasNotSubject() {
        User teacher = buildTeacher();
        Subject maths = new Subject("Математика");
        maths.setId(1L);
        teacher.getSubjects().add(maths);
        Subject physics = new Subject("Физика");
        physics.setId(2L);

        when(userRepo.findByIdWithRoles(1L)).thenReturn(Optional.of(teacher));
        when(subjectRepo.findById(2L)).thenReturn(Optional.of(physics));

        assertThrows(TeacherHasNotSubjectException.class,
                () -> userService.removeSubjectFromTeacher(1L, 2L));

        assertThat(teacher.getSubjects())
                .containsExactly(maths);
    }

    @Test
    public void getUsersByRole_shouldReturnPageOfUsers() {
        User teacher = buildTeacher();
        Pageable pageable = PageRequest.of(0, 20, Sort.by("id"));
        Page<User> page = new PageImpl<>(List.of(teacher), pageable, 1);

        when(userRepo.findAllByRoleName(RoleName.TEACHER, pageable))
                .thenReturn(page);

        Page<UserResponse> result = userService.getUsersByRole(RoleName.TEACHER, pageable);
        UserResponse response = result.getContent().get(0);

        assertThat(result.getContent()).hasSize(1);
        assertThat(response.email()).isEqualTo(teacher.getEmail());
        assertThat(response.firstName()).isEqualTo(teacher.getFirstName());
        assertThat(response.roles()).contains(RoleName.TEACHER);

        assertThat(result.getNumber()).isEqualTo(0);
        assertThat(result.getSize()).isEqualTo(20);
    }

    @Test
    public void getUsersByRole_shouldReturnEmptyPageOfUsers() {
        Pageable pageable = PageRequest.of(0, 20, Sort.by("id"));
        Page<User> page = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(userRepo.findAllByRoleName(RoleName.TEACHER, pageable))
                .thenReturn(page);

        Page<UserResponse> result = userService.getUsersByRole(RoleName.TEACHER, pageable);

        assertThat(result).isEmpty();
        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getTotalPages()).isZero();
    }

    private User buildTeacher() {
        Role teacherRole = Role.builder()
                .id(2L)
                .name(RoleName.TEACHER)
                .build();
        return User.builder()
                .id(2L)
                .email("teacher@test.com")
                .firstName("Иван")
                .lastName("Иванов")
                .roles(Set.of(teacherRole))
                .build();
    }

    private User buildStudent() {
        Role studentRole = Role.builder()
                .id(1L)
                .name(RoleName.STUDENT)
                .build();

        return User.builder()
                .id(1L)
                .email("test@test.com")
                .password("hash_password")
                .firstName("Vasia")
                .lastName("Vasilkov")
                .middleName("Zagagulivich")
                .roles(Set.of(studentRole))
                .build();
    }
}
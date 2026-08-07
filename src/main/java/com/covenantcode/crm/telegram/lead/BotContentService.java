package com.covenantcode.crm.telegram.lead;

import com.covenantcode.crm.entity.Course;
import com.covenantcode.crm.entity.enums.CourseStatus;
import com.covenantcode.crm.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class BotContentService {

    private final CourseRepository courseRepository;

    private final List<String> tips = List.of(
            """
            💡 Совет дня:
            
            Изучи Git с первого дня.
            Даже если проект небольшой — коммить каждое изменение.
            
            Базовый цикл:
            git init
            git add .
            git commit -m "first commit"
            
            Это навык №1 для любого разработчика. 🚀
            """,

            """
            💡 Совет дня:
            
            Читаемый код важнее «умного» кода.
            
            Называй переменные так, чтобы через год было понятно, зачем они нужны.
            Хороший код читается почти как обычный текст.
            """,

            """
            💡 Совет дня:
            
            Не пытайся выучить всё сразу.
            
            Выбери один язык, один фреймворк и дойди до первого рабочего проекта.
            Практика быстрее превращает знания в навык.
            """,

            """
            💡 Совет дня:
            
            Учись читать ошибки.
            
            Stack trace — это не страшный текст, а подсказка.
            Обычно в нём уже написано, какой класс, метод или строка вызвали проблему.
            """,

            """
            💡 Совет дня:
            
            Делай маленькие проекты.
            
            ToDo-лист, CRM, блог, Telegram-бот, REST API — всё это отлично подходит для портфолио начинающего разработчика.
            """,

            """
            💡 Совет дня:
            
            Пиши тесты хотя бы на основную бизнес-логику.
            
            JUnit 5 помогает проверить код до того, как ошибку найдёт пользователь.
            """,

            """
            💡 Совет дня:
            
            Изучи HTTP.
            
            Методы GET, POST, PUT, PATCH, DELETE, статус-коды 200, 201, 400, 401, 403, 404, 500 — база для backend-разработчика.
            """
    );

    public String getRandomTip() {
        int index = ThreadLocalRandom.current().nextInt(tips.size());
        return tips.get(index);
    }

    public String getFaq() {
        return """
                ❓ Часто задаваемые вопросы:
                
                Q: Нужен ли опыт программирования?
                A: Нет, базовые курсы рассчитаны на новичков.
                
                Q: Как проходят занятия?
                A: Онлайн, обычно 2 раза в неделю.
                
                Q: Можно ли совмещать обучение с работой?
                A: Да, занятия проходят в удобное вечернее время.
                
                Q: Будут ли практические задания?
                A: Да, обучение строится вокруг практики и проектов.
                
                Q: Помогаете ли вы с резюме?
                A: Да, помогаем оформить резюме и подготовить портфолио.
                
                Чтобы оставить заявку, нажмите кнопку «📝 Оставить заявку» или отправьте команду /apply.
                """;
    }

    public String formatCourses() {
        List<Course> courses = findActiveCourses();

        if (courses.isEmpty()) {
            return """
                🎓 Сейчас список активных курсов обновляется.
                
                Вы можете оставить заявку, и менеджер расскажет об актуальных направлениях.
                
                Чтобы оставить заявку → /apply
                """;
        }

        StringBuilder builder = new StringBuilder();

        builder.append("🎓 Наши курсы:\n\n");

        for (int i = 0; i < courses.size(); i++) {
            Course course = courses.get(i);

            builder.append(i + 1)
                    .append(". ")
                    .append(course.getTitle());

            if (course.getDurationInWeeks() > 0) {
                builder.append(" — ")
                        .append(course.getDurationInWeeks())
                        .append(" недель");
            }

            if (course.getPrice() != null) {
                builder.append(", ")
                        .append(course.getPrice())
                        .append(" ₽");
            }

            builder.append("\n");
        }

        builder.append("\nЧтобы записаться → /apply");

        return builder.toString();
    }

    public List<String> getActiveCourseNames() {
        List<Course> courses = findActiveCourses();

        List<String> names = new ArrayList<>();

        for (Course course : courses) {
            names.add(course.getTitle());
        }

        names.add("Другой курс");

        return names;
    }

    public Optional<Course> findCourseByTitle(String title) {
        if (title == null || title.isBlank()) {
            return Optional.empty();
        }

        return courseRepository.findByTitleIgnoreCase(title.trim());
    }

    private List<Course> findActiveCourses() {
        return courseRepository.findAllByStatus(CourseStatus.ACTIVE, Pageable.unpaged()).getContent();
    }
}

package dao;

import model.Child;
import org.junit.jupiter.api.*;
import utils.DBUtil;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Child Database Operations Tests")
class ChildDBTest {

    private ChildDB db;

    @BeforeEach
    void setUp() throws SQLException, IOException {
        new DBUtil().executeFile("init.sql");
        db = new ChildDB();
    }

    @AfterEach
    void tearDown() throws Exception {
        try (Connection conn = DBUtil.getConnection();
             Statement st = conn.createStatement()) {
            st.execute("TRUNCATE TABLE child CASCADE");
        }
        db.close();
    }

    @Test
    @DisplayName("Should add a child and return it with an ID")
    void addShouldAddChildAndReturnWithId() throws SQLException {
        // Arrange
        String firstName = "John";
        String lastName = "Doe";
        LocalDate birthDate = LocalDate.of(2010, 1, 1);
        Child child = new Child(firstName, lastName, birthDate);

        // Act
        Child addedChild = db.addChild(child);
        System.out.println("[DEBUG_LOG] Added child ID: " + addedChild.id());

        // Assert
        assertNotNull(addedChild.id(), "Child ID should not be null");
        assertEquals(firstName, addedChild.firstName(), "First name should match");
        assertEquals(lastName, addedChild.lastName(), "Last name should match");
        assertEquals(birthDate, addedChild.birthDate(), "Birth date should match");
    }

    @Test
    @DisplayName("Should add a child with null birth date")
    void addShouldHandleNullBirthDate() throws SQLException {
        Child child = new Child("John", "Second", null);
        Child addedChild = db.addChild(child);

        assertNotNull(addedChild.id(), "Child ID should not be null");
        assertNull(addedChild.birthDate(), "Birth date should be null");
    }

    @Test
    @DisplayName("Should update an existing child")
    void updateShouldUpdateExistingChild() throws SQLException {
        Child child = new Child("John", "Doe", LocalDate.of(2010, 1, 1));
        Child addedChild = db.addChild(child);

        Child updateChild = new Child(addedChild.id(), "Anna", "Koli", LocalDate.of(2011, 2, 2));

        boolean result = db.updateChild(updateChild);

        assertTrue(result, "Update should return true");

    }


    @Test
    @DisplayName("Should delete an existing child")
    void deleteShouldDeleteExistingChild() throws SQLException {
        Child child = new Child("John", "Doe", LocalDate.of(2010, 1, 1));
        Child addedChild = db.addChild(child);

        boolean deleteResult = db.deleteChild(addedChild.id());

        assertTrue(deleteResult, "Delete operation should return true");
    }


    @Test
    @DisplayName("Should return children with at least the specified age")
    void findChildrenWithMinimumAgeShouldReturnChildrenWithMinimumAge() throws SQLException {
        Child child1 = new Child("Kate", "First", LocalDate.of(2016, 2, 2));
        Child child2 = new Child("Stiv", "Second", LocalDate.of(2021, 3, 3));
        Child child3 = new Child("John", "Doe", LocalDate.of(2011, 4, 4));
        db.addChild(child1);
        db.addChild(child2);
        db.addChild(child3);
        List<Child> children = db.findChildrenWithMinimumAge(10);
        assertTrue(children.size() >= 2, "Should find at least 2 children");
        assertTrue(children.stream().anyMatch(c -> "Kate".equals(c.firstName())));
        assertTrue(children.stream().anyMatch(c -> "John".equals(c.firstName())));
    }

    @Test
    @DisplayName("Should return children with null birth date")
    void findChildrenWithoutBirthDateShouldReturnChildrenWithNullBirthDate() throws SQLException {
        Child child1 = new Child("Bob", "Thorn", LocalDate.of(2010, 2, 2));
        Child child2 = new Child("Nik", "Dark", null);
        db.addChild(child1);
        db.addChild(child2);

        List<Child> children = db.findChildrenWithoutBirthDate();
        assertTrue(children.stream().anyMatch(c -> "Nik".equals(c.firstName()) && c.birthDate() == null));

    }
}


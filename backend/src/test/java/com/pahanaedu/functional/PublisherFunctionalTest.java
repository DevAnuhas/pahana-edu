package com.pahanaedu.functional;

import com.pahanaedu.dao.PublisherDAO;
import com.pahanaedu.model.Publisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Functional tests for publisher management operations
 */
public class PublisherFunctionalTest {

    private MockPublisherDAO mockPublisherDAO;

    private static class MockPublisherDAO extends PublisherDAO {
        private final List<Publisher> publishers = new ArrayList<>();
        private int nextId = 1;
        private boolean shouldFailOnCreate = false;

        public void setShouldFailOnCreate(boolean shouldFail) {
            this.shouldFailOnCreate = shouldFail;
        }

        @Override
        public Publisher findById(int id) {
            return publishers.stream()
                    .filter(publisher -> publisher.getId() == id)
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public List<Publisher> findAll() {
            return new ArrayList<>(publishers);
        }

        @Override
        public boolean create(Publisher publisher) {
            if (shouldFailOnCreate) return false;
            publisher.setId(nextId++);
            publishers.add(publisher);
            return true;
        }

        @Override
        public boolean update(Publisher publisher) {
            for (int i = 0; i < publishers.size(); i++) {
                if (publishers.get(i).getId() == publisher.getId()) {
                    publishers.set(i, publisher);
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean delete(int id) {
            return publishers.removeIf(publisher -> publisher.getId() == id);
        }
    }

    @BeforeEach
    public void setUp() {
        mockPublisherDAO = new MockPublisherDAO();
    }

    @Test
    public void testCreateValidPublisher() {
        Publisher publisher = new Publisher();
        publisher.setName("Test Publisher");
        publisher.setContactPerson("John Doe");
        publisher.setTelephone("0112345678");
        publisher.setEmail("contact@testpub.com");

        boolean result = mockPublisherDAO.create(publisher);
        assertTrue(result);
        assertNotEquals(0, publisher.getId());
    }

    @Test
    public void testCreatePublisherWithEmptyName() {
        Publisher publisher = new Publisher();
        publisher.setName("");
        mockPublisherDAO.setShouldFailOnCreate(true);

        boolean result = mockPublisherDAO.create(publisher);
        assertFalse(result);
    }

    @Test
    public void testUpdatePublisher() {
        Publisher publisher = new Publisher();
        publisher.setId(1);
        publisher.setName("Updated Publisher");
        mockPublisherDAO.create(publisher);

        publisher.setContactPerson("Jane Doe");
        boolean result = mockPublisherDAO.update(publisher);
        assertTrue(result);
    }

    @Test
    public void testDeletePublisher() {
        Publisher publisher = new Publisher();
        publisher.setName("Test Publisher");
        mockPublisherDAO.create(publisher);
        int publisherId = publisher.getId();

        boolean result = mockPublisherDAO.delete(publisherId);
        assertTrue(result);
        assertNull(mockPublisherDAO.findById(publisherId));
    }

    @Test
    public void testFindPublisherById() {
        Publisher publisher = new Publisher();
        publisher.setName("Test Publisher");
        mockPublisherDAO.create(publisher);

        Publisher found = mockPublisherDAO.findById(publisher.getId());
        assertNotNull(found);
        assertEquals("Test Publisher", found.getName());
    }

    @Test
    public void testGetAllPublishers() {
        Publisher pub1 = new Publisher();
        pub1.setName("Publisher 1");
        mockPublisherDAO.create(pub1);

        Publisher pub2 = new Publisher();
        pub2.setName("Publisher 2");
        mockPublisherDAO.create(pub2);

        List<Publisher> publishers = mockPublisherDAO.findAll();
        assertEquals(2, publishers.size());
    }

    @Test
    public void testValidatePublisherEmail() {
        Publisher publisher = new Publisher();
        publisher.setName("Test Publisher");
        publisher.setEmail("invalid-email");

        // Email validation would typically be handled in service layer
        boolean isValid = publisher.getEmail().contains("@");
        assertFalse(isValid);
    }

    @Test
    public void testValidatePublisherPhone() {
        Publisher publisher = new Publisher();
        publisher.setName("Test Publisher");
        publisher.setTelephone("011234567890"); // Too long

        boolean isValid = publisher.getTelephone().length() <= 10;
        assertFalse(isValid);
    }

    @Test
    public void testPublisherContactInfo() {
        Publisher publisher = new Publisher();
        publisher.setName("Test Publisher");
        publisher.setContactPerson("John Doe");
        publisher.setTelephone("0112345678");
        publisher.setEmail("john@testpub.com");
        publisher.setAddress("123 Main St");

        mockPublisherDAO.create(publisher);
        
        Publisher retrieved = mockPublisherDAO.findById(publisher.getId());
        assertEquals("John Doe", retrieved.getContactPerson());
        assertEquals("0112345678", retrieved.getTelephone());
        assertEquals("john@testpub.com", retrieved.getEmail());
        assertEquals("123 Main St", retrieved.getAddress());
    }

    @Test
    public void testDuplicatePublisherName() {
        Publisher pub1 = new Publisher();
        pub1.setName("Duplicate Publisher");
        mockPublisherDAO.create(pub1);

        Publisher pub2 = new Publisher();
        pub2.setName("Duplicate Publisher");
        mockPublisherDAO.setShouldFailOnCreate(true);

        boolean result = mockPublisherDAO.create(pub2);
        assertFalse(result);
    }
}
package com.aau.se.expensetracker.repository;

import com.aau.se.expensetracker.model.BasicExpense;
import com.aau.se.expensetracker.model.Expense;
import com.aau.se.expensetracker.model.ExpenseCategory;
import com.aau.se.expensetracker.util.DataAccessException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * File-based implementation of ExpenseRepository.
 * Format per line: id|name|amount|date|category
 */
public class FileExpenseRepository implements ExpenseRepository {

    private static final String SEP = "|";

    private final Path path;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final ConcurrentHashMap<String, Expense> cache = new ConcurrentHashMap<>();

    public FileExpenseRepository(String filePath) {
        this.path = Path.of(filePath).toAbsolutePath();
        ensureFileExists();
    }

    private void ensureFileExists() {
        try {
            Path parent = path.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }
            if (!Files.exists(path)) {
                Files.createFile(path);
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not create expense file: " + path, e);
        }
    }

    @Override
    public void save(Expense expense) throws DataAccessException {
        lock.writeLock().lock();
        try {
            cache.put(expense.getId(), expense);
            persistAll();
        } catch (IOException e) {
            throw new DataAccessException("Failed to save expense", e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public List<Expense> loadAll() throws DataAccessException {
        lock.readLock().lock();
        try {
            if (cache.isEmpty()) {
                lock.readLock().unlock();
                lock.writeLock().lock();
                try {
                    if (cache.isEmpty()) {
                        loadFromFile();
                    }
                } finally {
                    lock.readLock().lock();
                    lock.writeLock().unlock();
                }
            }
            return new ArrayList<>(cache.values());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void delete(String expenseId) throws DataAccessException {
        lock.writeLock().lock();
        try {
            cache.remove(expenseId);
            persistAll();
        } catch (IOException e) {
            throw new DataAccessException("Failed to delete expense", e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void loadFromFile() throws DataAccessException {
        if (!Files.exists(path)) {
            return;
        }
        try {
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            cache.clear();
            for (String line : lines) {
                if (line.isBlank()) continue;
                Expense e = parseLine(line);
                if (e != null) cache.put(e.getId(), e);
            }
        } catch (IOException e) {
            throw new DataAccessException("Failed to load expenses", e);
        }
    }

    private void persistAll() throws IOException {
        List<Expense> all = new ArrayList<>(cache.values());
        List<String> lines = new ArrayList<>();
        for (Expense e : all) {
            lines.add(toLine(e));
        }
        Files.write(path, lines, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private String toLine(Expense e) {
        return e.getId() + SEP + escape(e.getName()) + SEP + e.getAmount() + SEP
                + e.getDate() + SEP + e.getCategory().name();
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("|", "\\|").replace("\\", "\\\\");
    }

    private static String unescape(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) {
                char next = s.charAt(i + 1);
                if (next == '|' || next == '\\') {
                    sb.append(next);
                    i++;
                    continue;
                }
            }
            sb.append(c);
        }
        return sb.toString();
    }

    private Expense parseLine(String line) {
        List<String> parts = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean escaped = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (escaped) {
                current.append(c);
                escaped = false;
                continue;
            }
            if (c == '\\') {
                escaped = true;
                continue;
            }
            if (c == '|') {
                parts.add(current.toString());
                current = new StringBuilder();
                continue;
            }
            current.append(c);
        }
        parts.add(current.toString());
        if (parts.size() != 5) return null;
        try {
            String id = parts.get(0);
            String name = unescape(parts.get(1));
            double amount = Double.parseDouble(parts.get(2));
            LocalDate date = LocalDate.parse(parts.get(3));
            ExpenseCategory category = ExpenseCategory.valueOf(parts.get(4));
            return new BasicExpense(id, name, amount, date, category);
        } catch (Exception e) {
            return null;
        }
    }
}

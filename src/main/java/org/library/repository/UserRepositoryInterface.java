package org.library.repository;

import org.library.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepositoryInterface {
    User save(User user);

    Optional<User> findById(Integer id);

    Optional<User> findByEmail(String email);

    List<User> findAll();

    Boolean delete(Integer id);

    Boolean update(User user);
}
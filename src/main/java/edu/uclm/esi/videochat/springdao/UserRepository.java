package edu.uclm.esi.videochat.springdao;

import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import edu.uclm.esi.videochat.model.User;

public interface UserRepository extends CrudRepository<User, String> {

	@Query(value = "SELECT count(*) FROM user where name=:name and pwd=:pwd", nativeQuery = true)
	public int checkPassword(@Param("name") String name, @Param("pwd") String pwd);

	public User findByNameAndPwd(String name, String pwd);

	public Optional<User> findByName(String name);

	@Query(value = "SELECT * FROM user where name=:name", nativeQuery = true)
	public User getImage(@Param("name") String name);

	@Modifying
	@Transactional
	@Query(value = "DELETE FROM user where name=:name", nativeQuery = true)
	public int deleteUser(@Param("name") String name);
}

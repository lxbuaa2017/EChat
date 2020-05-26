package com.example.echatbackend.service;

import com.example.echatbackend.dao.GroupRepository;
import com.example.echatbackend.dao.GroupUserRepository;
import com.example.echatbackend.dao.UserRepository;
import com.example.echatbackend.entity.Group;
import com.example.echatbackend.entity.GroupUser;
import com.example.echatbackend.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GroupService extends BaseService<Group, Integer, GroupRepository> {

    private final GroupUserRepository groupUserRepository;
    private final UserRepository userRepository;

    @Autowired
    public GroupService(GroupUserRepository groupUserRepository, UserRepository userRepository) {
        this.groupUserRepository = groupUserRepository;
        this.userRepository = userRepository;
    }

    public Group findGroupById(int id) {
        Optional<Group> group = baseRepository.findById(id);
        if (group.isEmpty()) {
            return null;
        }
        return group.get();
    }

    public User[] findUserByGroup(Group group) {
        List<GroupUser> groupUsers = groupUserRepository.findAllByGroup(group);
        return groupUsers.stream().map(GroupUser::getUser).toArray(User[]::new);
    }

    public List<Group> searchGroupByCode(String keyword, int offset, int limit) {
        Specification<Group> groupSpecification = (Specification<Group>) (root, criteriaQuery, cb) -> cb.like(root.get("code"), "%" + keyword + "%");
        Sort sort = Sort.by(Sort.Order.asc("code"));
        return baseRepository.findAll(groupSpecification, PageRequest.of(offset, limit, sort)).getContent();
    }

    public List<Group> searchGroupByName(String keyword, int offset, int limit) {
        Specification<Group> groupSpecification = (Specification<Group>) (root, criteriaQuery, cb) -> cb.like(root.get("name"), "%" + keyword + "%");
        Sort sort = Sort.by(Sort.Order.asc("name"));
        return baseRepository.findAll(groupSpecification, PageRequest.of(offset, limit, sort)).getContent();
    }
}

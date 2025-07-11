package com.example.onlinecoursescatalog.service;

import com.example.onlinecoursescatalog.model.Tag;
import com.example.onlinecoursescatalog.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TagService {

    private final TagRepository tagRepository;

    @Autowired
    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }

    public Optional<Tag> getTagByName(String name) {
        return tagRepository.findByName(name.trim());
    }

    public Tag saveTag(Tag tag) {
        tag.setName(tag.getName().trim());
        return tagRepository.save(tag);
    }

    public Optional<Tag> getTagById(Long id) {
        return tagRepository.findById(id);
    }
}
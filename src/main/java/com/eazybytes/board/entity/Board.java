package com.eazybytes.board.entity;

import com.eazybytes.entity.BaseEntity;
import com.eazybytes.task.entity.Task;
import com.eazybytes.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.List;
import java.util.UUID;

@Entity(name = "boards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Board extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "title", nullable = false, length = 50)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "icon", length = 50)
    @ColumnDefault("default")
    private String icon;

    @Column(name = "color", length = 50)
    @ColumnDefault("default")
    private String color;

    @Column(name = "is_archived")
    private Boolean isArchived = false;

    @OneToMany(mappedBy = "board", fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Task> tasks;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

}
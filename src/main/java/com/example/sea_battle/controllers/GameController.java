package com.example.sea_battle.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Контроллер игры", description = "Содержит методы для ведения игры")
@RestController
@RequestMapping("/game")
public class GameController {
}

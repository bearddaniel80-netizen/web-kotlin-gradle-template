package example.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
class DefaultController {

  @GetMapping("/")
  fun index(): String {
      return "forward:/index.html"
  }
}
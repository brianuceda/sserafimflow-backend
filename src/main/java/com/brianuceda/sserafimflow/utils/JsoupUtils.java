package com.brianuceda.sserafimflow.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import com.brianuceda.sserafimflow.exceptions.GeneralExceptions.*;

@Component
public class JsoupUtils {
  // Conectar a una URL con JSoup
  public Document connect(String url) {
    try {
      // simular un usuario real
      return Jsoup.connect(url).execute().parse();
    } catch (Exception ex) {
      throw new ConnectionFailed(ex.getMessage());
    }
  }
}

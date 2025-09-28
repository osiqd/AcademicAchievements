module coursework.univer {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;
    requires java.sql;
    requires java.net.http;

    requires com.fasterxml.jackson.databind;
    requires com.google.auth.oauth2; // если используешь Google Auth

    // Не подключаем Firebase Admin как модуль, он на classpath
    // requires firebase.admin; // ❌ убираем

    exports coursework.univer;                  // главный пакет с MainApplication
    exports coursework.univer.controller;
    exports coursework.univer.model;
    exports coursework.univer.service;
    exports coursework.univer.dao;

    opens coursework.univer.controller to javafx.fxml;
    opens coursework.univer.model to javafx.base, javafx.fxml;
}

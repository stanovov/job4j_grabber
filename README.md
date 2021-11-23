[![Build Status](https://travis-ci.com/stanovov/job4j_grabber.svg?branch=master)](https://travis-ci.com/stanovov/job4j_grabber)

![](https://img.shields.io/badge/Maven-=_3-red)
![](https://img.shields.io/badge/Java-=_14-orange)
![](https://img.shields.io/badge/JDBC-738bff)
![](https://img.shields.io/badge/PostgerSQL-=_9-blue)
![](https://img.shields.io/badge/Checkstyle-lightgrey)

# job4j_grabber

+ [О проекте](#0-проекте)
+ [Технологии](#Технологии)
+ [Сборка и запуск](#Сборка-и-запуск)
+ [Использование](#Использование)
+ [Контакты](#Контакты)

## О проекте

Система запускается по расписанию. Период запуска указывается в настройках - grabber.properties. Программа считывает все 
вакансии с сайта [sql.ru](sql.ru) относящиеся к Java и записывает их в базу. Отображает вакансии
в порядке убывания по дате создания.

## Технологии 

+ Сборщик проектов **Maven**;
+ Backend - **Java 14**, **jsoup**;
+ Логгирование - **Log4j**, **Slf4j**;
+ СУБД - **PostgreSQL**;
+ Непрерывная интеграция - **Travis CI**;
+ Инструмент для анализа стиля кода - **Checkstyle**;

## Сборка и запуск

### Запуск через терминал

1.Собрать jar через Maven

`mvn install -Dmaven.test-skip=true`

2.Запустить jar файл

`java -jar target/grabber.jar`

### Запуск через IDE

Перейти к папке `src/main/java` и файлу `ru.job4j.grabber.Grabber`

## Использование

![Posts](images/grabber.png)

## Контакты

Становов Семён Сергеевич

Email: sestanovov@gmail.com

Telegram: [@stanovovss](https://t.me/stanovovss)
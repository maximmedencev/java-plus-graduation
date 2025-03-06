# Архитектура проекта.
Проект включает в себя сервисы:
1. event-and-additional-service для работы с категориями, событиями и компиляциями событий.
2. request-service для работы с запросами на участие и т.п.
3. subscription-service для работы с подписками.
4. user-service для управлениями данными пользователей.

# Внутренний API для взаимодействия сервисов
Cервисы взаимодействуют друг с другом при помощи FeignClient.
Внутренний API:

## event-and-additional-service
**GET /events/feign/{eventId}/{userId}** - получение EventFullDto для события с id={eventId} и инициатора с id = {userId}
**GET /events/feign{eventId}** - получение EventFullDto для события с id={eventId}

## request-service
**GET/requests/confirmed) Map<Long, Long> (@RequestParam List<Long> eventIds)** -  для получения карты, где ключом является идентификатор события (eventId), а значением — количество подтвержденных запросов на участие в этом событии
**GET(/requests/count/{eventId}/{requestStatus}) Long** - для получения количества запросов по запросов с id = {eventId}, и со статусом {requestStatus}

## user-service
**POST(/admin/users) create(@RequestParam email, @RequestParam name)** - создание пользователя по имени и почте
**GET(/admin/users) List<UserDto> findAllBy(@RequestParam(required = false) List<Long> ids,
@RequestParam(defaultValue = "0") int from,
@RequestParam(defaultValue = "10") int size)** - поиск пользователей по списку их id
**DELETE(/admin/users/{userId}) deleteBy(@PathVariable Long userId)** - удаление пользователя по id
**GET(/admin/users/mapped) Map<Long, UserShortDto> userMapBy(@RequestParam List<Long> ids)** - получение карты, в которой  ключ это идентификатор пользователя, а значение UserShortDto

# Конфигурации
Конфигурации сервисов находятся в сервисе infra/config-server (Spring Cloud Config Server)
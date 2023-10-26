# Змейка по сети

Реализовать многопользовательскую игру "Змейка".
Управляя своей змейкой, нужно поедать "еду" и расти, при этом избежав столкновения с "телом" своей змейки и со змейками, управляемыми другими игроками.

Все программы студентов должны реализовывать одинаковый протокол и быть совместимыми между собой.
Описание протокола - в файле [snakes.proto](src/main/proto/snakes.proto).
Далее упоминается конфиг игры, все параметры конфига задаются перед стартом игры и после этого не меняются.
Возможные значения параметров указаны в файле описания протокола.

Баллов за задачу: 5
Дедлайн для групп Ипполитова: 15 ноября 2023

## Правила игры

Игровое поле - прямоугольник со сторонами, заданными в конфиге игры.
Горизонтальные ряды клеток назовём строками, вертикальные - столбцами.
При этом поле логически замкнуто с противоположных сторон: с самой левой клетки строки можно перейти влево на самую правую клетку той же строки, аналогично для самой верхней клетки столбца (математически это дискретный тор).
Змейка представляет собой последовательность клеток на игровом поле, образующих непрерывную ломаную линию с прямыми углами изгиба (см. рисунок).
Таким образом, змейка может пересекать границы поля неограниченное число раз.
Клетка на одном из концов змейки - голова, направлением движения которой управляет игрок, клетка на противоположном конце - хвост.
На поле могут быть расположены одна или более змеек, каждой змейкой управляет один игрок.
Также на поле в каждый момент времени присутствует еда в количестве, вычисляемом по формуле (food_static + (число ALIVE-змеек)).
Каждая еда занимает 1 клетку.
На одной клетке не может находиться две еды.
Еда на поле размещается случайным образом на пустых клетках.
Если на текущем ходе на поле не хватает клеток, чтобы разместить требуемое количество еды, то размещается столько, сколько возможно.

Время в игре дискретное, состояния сменяются через интервал реального времени в миллисекундах, заданный в конфиге.
За время между сменами состояния (за один "ход") у игроков есть возможность сменить направление головы своей змейки с помощью команды SteerMsg.
Если команда заставляет змейку повернуть в направлении, занятом соседней с головой клеткой (попытка повернуть вспять), то такая команда игнорируется.
Игрок может успеть заменить предыдущую команду путём отправки новой команды, но если команда не успела дойти до момента смены состояния, то она применяется на следующем ходу.

В момент смены состояния голова каждой змейки продвигается на 1 клетку в текущем своём направлении.
Если клетка, куда перешла голова, содержала еду, то остальные клетки змеи не сдвигаются, а игрок зарабатывает +1 балл.
А если еды не было, то все последующие клетки змейки продвигаются на место предыдущих, а клетка, на которой находился хвост, освобождается.
Далее проверяется получившееся состояние целевой клетки, куда перешли головы змеек.
Если целевая клетка занята змейкой (своим телом, хвостом или любой чужой клеткой), то "врезавшаяся" змейка погибает, а игрок выбывает из игры.
Клетки, которые занимает погибшая змейка (после перемещения головы), с вероятностью 0.5 превращаются в еду, а остальные клетки становятся пустыми.
Змейка, в которую "врезались", зарабатывает +1 балл (если она не врезалась сама в себя, конечно).
Змейка погибает независимо от того, погибает ли на этом же ходу змейка, в которую она "врезалась".
Если на одну и ту же клетку наехало несколько голов, то все эти змейки погибают таким же образом.

Таким образом можно, например, вплотную преследовать свой собственный (или чужой хвост), не погибая.
Но если при этом чужая змейка съест еду, то мы на этом ходу "врежемся" в её хвост.
В случае если на клетке была еда, и на неё наехало несколько голов одновременно, то еда съедается, хвосты всех этих змеек остаются на месте, но потом они все умирают.

## Логика работы программы

Необходимо реализовать программу, которая будет позволять игроку:
1. Начать новую игру либо присоединиться к имеющейся игре.
1. Играть.
1. Выйти из игры.

Взаимодействие программ происходит по протоколу UDP по принципу peer-to-peer, формат сообщений описан в файле snakes.proto.
В тело каждого UDP-сообщения пакуется ровно одно сообщение типа GameMessage, без каких-либо лишних байтов.
Всё взаимодействие нужно реализовать через два UDP-сокета: один для приёма multicast-сообщений (но не для их отправки!), второй для всего остального.
Второй сокет bind-ится на выбранный операционной системой порт.
Такая организация работы удобна для тестирования нескольких копий программы на одной машине, т.к. они будут использовать разные порты и не будут мешать друг другу.
Пользователь, который начинает новую игру, имеет возможность задать параметры конфига игры в пределах допустимых значений, указанных в proto-файле.
Способ ввода этих параметров может быть любым разумным (например, ввод из интерфейса или перечисление в текстовом файле).
После этого начинается игра с единственной змейкой этого игрока на поле, все правила действуют как обычно.
При этом узел этого игрока становится главным (MASTER).
Узел с ролью MASTER рассылает сообщения AnnouncementMsg с интервалом в 1 секунду на multicast-адрес 239.192.0.4, порт 9192.
Также узел с ролью MASTER отвечает на сообщение DiscoverMsg сообщением AnnouncementMsg (без подтверждений).

Все остальные сообщения в игре рассылаются unicast-ом.
Поскольку узел не может в общем случае с уверенностью узнать собственный IP-адрес и порт (например, в случае NAT), то в объекте GamePlayer в описании игрока-отправителя отсутствуют поля ip_address и port.
В этих случаях, адрес и порт этого узла должны браться из информации об отправителе UDP-пакета.

Другие узлы принимают сообщения AnnouncementMsg и отображают список идущих игр в интерфейсе (когда игрок не занят игрой, либо всегда).
Пользователь может присоединиться к идущей игре вместо того, чтобы начинать новую.

При этом он должен принять её конфиг, и не может на него повлиять.
Для присоединения к игре отправляется сообщение JoinMsg узлу, от которого было получено сообщение AnnouncementMsg.
В этом сообщении указывается имя игры, к которой хотим присоединиться (в текущей версии задачи это неважно, оставлено на будущее).
Также указывается режим присоединения, стандартный или "только просмотр", во втором случае игрок не получает змейку, а может только просматривать, что происходит на поле.
Когда к игре присоединяется новый игрок, MASTER-узел находит на поле квадрат 5x5 клеток, в котором нет клеток, занятых змейками.
Квадрат ищется с учётом того, что края поля замкнуты.
Для нового игрока создаётся змейка длиной две клетки, её голова помещается в центр найденного квадрата, а хвост - случайным образом в одну из четырёх соседних клеток.
На двух клетках, которые займёт новая змейка, не должно быть еды, иначе ищется другое расположение.
Исходное направление движения змейки противоположно выбранному направлению хвоста.
Число очков присоединившегося игрока равно 0.
Если не удалось найти подходящий квадрат 5x5, то пытающемуся присоединиться игроку отправляется ErrorMsg с сообщением об ошибке.
Если удалось разместить новую змейку на поле, то новому игроку присваивается уникальный идентификатор в пределах текущего состояния игры.
В ответ на JoinMsg отправляется сообщение AckMsg, в котором игроку сообщается его идентификатор в поле receiver_id.

В итоге узлы, участвующие в одной игре, образуют топологию "звезда": в центре узел с ролью MASTER, остальные узлы является непосредственными его соседями.

За текущее состояние игры отвечает центральный узел, и все изменения в него вносит только он.
Когда игрок, не являющийся центральным, хочет повернуть голову своей змейки, он отправляет сообщение SteerMsg центральному узлу.
Центральный узел накапливает все такие изменения, причём более новое изменение заменяет собой более старое в пределах хода (порядок определяется по msg_seq в сообщениях).
Например, если игрок полз вверх, нажал влево, а потом передумал и нажал вправо, и оба сообщения успели дойти и обработаться до смены хода, то змейка на следующем ходу поползёт вправо.
Когда наступает время перехода на следующий ход, центральный узел применяет все эти повороты, продвигает змей и заменяет съеденную еду на новую в соответствии с правилами игры.
После этого новое состояние рассылается всем игрокам в сообщении StateMsg.
Если узел получил состояние, в котором state_order меньше либо равен тому, который уже известен узлу, то содержимое такого сообщения не обрабатывается.

Любое сообщение (кроме AnnouncementMsg, DiscoverMsg и AckMsg) подтверждается путём отправки в ответ сообщения AckMsg с таким же msg_seq, как в исходном сообщении.
Если отправитель не получил такое подтверждение, он переотправляет сообщение через интервал, равный значению параметра state_delay_ms делённому на 10.
Если мы не отправляли абсолютно никаких unicast-сообщений узлу в течение интервала из предыдущего пункта, то необходимо отправить сообщение PingMsg.

Если мы не получали абсолютно никаких unicast-сообщений от узла в течение 0.8 * state_delay_ms миллисекунд, то мы считаем что узел выпал из игры.
Тут может быть три ситуации:
а) Узел с ролью NORMAL заметил, что отвалился MASTER. Тогда он заменяет информацию о центральном узле на заместителя (DEPUTY), т.е начинает посылать все unicast-сообщения в сторону DEPUTY.
б) Узел с ролью MASTER заметил, что отвалился DEPUTY. Тогда он выбирает нового DEPUTY среди NORMAL-ов, и сообщает об этом самому DEPUTY сообщением RoleChangeMsg (остальные узнают о новом DEPUTY из планового StatusMsg, им это знать не срочно).
в) Узел с ролью DEPUTY заметил, что отвалился MASTER. Тогда он сам становится MASTER-ом (берёт управление игрой на себя), выбирает нового DEPUTY, и сообщает об этом каждому игроку сообщением RoleChangeMsg.

Важно, что при смене MASTER-а сообщения, ранее отправленные старому MASTER-у, но ещё не подтверждённые, должны продолжить переотправляться новому MASTER-у.
Узел с ролью VIEWER не может одновременно иметь роль MASTER или DEPUTY (это может кому-то показаться нелогичным, но это так в текущей версии протокола).

Игрок может выйти из игры как по таймауту (см. выше), так и явно с помощью сообщения RoleChangeMsg, указав что желает стать VIEWER-ом.
В обоих случаях он теряет контроль над своей змейкой, и её SnakeState меняется на ZOMBIE.
Такая змейка не исчезает, а продолжает двигаться в неизменном направлении, соблюдая все правила игры: может съесть еду, врезаться и погибнуть, и т.д.

Игра завершается, когда из неё выходит последний игрок (MASTER).
Если в этот момент были подключены узлы с ролью VIEWER, допускается не обрабатывать на них такую ситуацию.
Информация о завершённой игре не обязана нигде сохраняться.

## Требования к внешнему виду и управлению

Для управления змейкой нужно предусмотреть способ задания направления змейки, который удобен для данного типа устройства.
В случае приложения для компьютера это могут быть клавиши на клавиатуре или геймпаде.
В случае мобильного приложения это могут быть кнопки на экране, свайпы в нужном направлении, или клавиши на внешнем геймпаде или клавиатуре.
Пример удобного способа: направление поворота задаётся нажатием на клавиатуре одной из клавиш: w a s d.
Пример неудобного способа: чтобы повернуть вверх, нужно нажать w, потом Enter.

Интерфейс программы должен предусматривать:
1. Показ списка идущих игр, полученных с помощью multicast-сообщений, с возможностью присоединения к любой из них.
1. Показ игрового поля и списка игроков с числом набранных баллов, с возможностью выйти из игры (на одном экране).
1. Возможность начать новую игру.

Всё это может быть как на одном экране, так и на нескольких, между которыми организованы переходы.
Интерфейс может быть как графическим, так и текстовым в терминале.
Отображаемый список игроков содержит всех играющих игроков (т.е. кроме тех, кто в режиме VIEWER).

## Требования к реализации

Количество тредов в программе в любом режиме её работы должно быть ограничено любым заранее известным числом.
Это означает что число тредов не должно зависеть от числа вещей (игроков, змей, еды, и т.п.), от числа перезапусков игры и пр.

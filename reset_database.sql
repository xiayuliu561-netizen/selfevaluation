/*
 Reset the selfevaluation database to its initial empty state.
 Only the super administrator account is kept:
   username: admin
   password: 123456
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE `assessrate`;
TRUNCATE TABLE `college`;
TRUNCATE TABLE `dept`;
TRUNCATE TABLE `lesson`;
TRUNCATE TABLE `news`;
TRUNCATE TABLE `question`;
TRUNCATE TABLE `questionscore`;
TRUNCATE TABLE `rate`;
TRUNCATE TABLE `reports`;
TRUNCATE TABLE `score`;
TRUNCATE TABLE `sumscore`;
TRUNCATE TABLE `ai_model`;
TRUNCATE TABLE `lesson_student`;
TRUNCATE TABLE `analysis_component`;
TRUNCATE TABLE `analysis_target`;
TRUNCATE TABLE `analysis_target_item`;
TRUNCATE TABLE `score_detail`;
TRUNCATE TABLE `system_statistics`;
TRUNCATE TABLE `user`;

INSERT INTO `user` VALUES
(1, NULL, 'admin', '123456', '超管', NULL, NULL, NULL, NULL, NULL, NULL, '0', NULL, NULL, NULL, NULL, NULL, NULL);

INSERT INTO `system_statistics` (`stat_key`, `stat_value`, `description`)
VALUES ('grade_service_count', 0, '累计成功处理成绩的学生人次');

SET FOREIGN_KEY_CHECKS = 1;

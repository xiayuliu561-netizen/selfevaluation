SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `system_statistics` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `stat_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `stat_value` bigint(20) NOT NULL DEFAULT 0,
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_system_statistics_key` (`stat_key`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 CHARACTER SET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=Dynamic;

INSERT IGNORE INTO `system_statistics` (`stat_key`, `stat_value`, `description`)
VALUES ('grade_service_count', 0, '累计成功处理成绩的学生人次');

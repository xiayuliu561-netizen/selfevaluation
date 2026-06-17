SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `lesson_student` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `lessonid` int(11) DEFAULT NULL,
  `userid` int(11) DEFAULT NULL,
  `teacherid` int(11) DEFAULT NULL,
  `createtime` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_lesson_student` (`lessonid`,`userid`,`teacherid`),
  KEY `idx_lesson_student_lesson` (`lessonid`),
  KEY `idx_lesson_student_user` (`userid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `analysis_component` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `lessonid` int(11) DEFAULT NULL,
  `teacherid` int(11) DEFAULT NULL,
  `component_name` varchar(255) DEFAULT NULL,
  `rate` decimal(11,2) DEFAULT 0.00,
  `sortno` int(11) DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_analysis_component_lesson` (`lessonid`,`teacherid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `analysis_target` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `lessonid` int(11) DEFAULT NULL,
  `teacherid` int(11) DEFAULT NULL,
  `target_name` varchar(255) DEFAULT NULL,
  `targetrate` decimal(11,2) DEFAULT 0.00,
  `sortno` int(11) DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_analysis_target_lesson` (`lessonid`,`teacherid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `analysis_target_item` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `targetid` int(11) DEFAULT NULL,
  `lessonid` int(11) DEFAULT NULL,
  `teacherid` int(11) DEFAULT NULL,
  `method_name` varchar(255) DEFAULT NULL,
  `weight_rate` decimal(11,2) DEFAULT 0.00,
  `coefficient` decimal(11,2) DEFAULT 0.00,
  `sortno` int(11) DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_analysis_target_item_target` (`targetid`),
  KEY `idx_analysis_target_item_lesson` (`lessonid`,`teacherid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `score_detail` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `lessonid` int(11) DEFAULT NULL,
  `teacherid` int(11) DEFAULT NULL,
  `userid` int(11) DEFAULT NULL,
  `component_name` varchar(255) DEFAULT NULL,
  `score` decimal(18,2) DEFAULT 0.00,
  `sortno` int(11) DEFAULT 0,
  `source_status` varchar(30) DEFAULT NULL,
  `createdate` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_score_detail` (`lessonid`,`teacherid`,`userid`,`component_name`),
  KEY `idx_score_detail_lesson_user` (`lessonid`,`teacherid`,`userid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

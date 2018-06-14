-- phpMyAdmin SQL Dump
-- version 4.2.7.1
-- http://www.phpmyadmin.net
--
-- Host: 127.0.0.1
-- Generation Time: 2016-04-17 04:55:30
-- 服务器版本： 5.5.36-log
-- PHP Version: 5.5.10

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Database: `smartplug`
--

-- --------------------------------------------------------

--
-- 表的结构 `module_info`
--

CREATE TABLE IF NOT EXISTS `module_info` (
  `module_id` varchar(32) NOT NULL,
  `module_name` varchar(32) NOT NULL,
  `protype` tinyint(1) unsigned NOT NULL COMMENT 'protocol type',
  `power_status` tinyint(1) unsigned NOT NULL,
  `mode` tinyint(1) unsigned NOT NULL,
  `red` tinyint(1) unsigned NOT NULL,
  `green` tinyint(1) unsigned NOT NULL,
  `blue` tinyint(1) unsigned NOT NULL,
  `cookie` varchar(32) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- 表的结构 `timer_info`
--

CREATE TABLE IF NOT EXISTS `timer_info` (
  `timer_id` tinyint(1) unsigned NOT NULL,
  `timer_type` tinyint(1) unsigned NOT NULL,
  `module_id` varchar(32) NOT NULL,
  `peroid` varchar(16) NOT NULL,
  `time_on` varchar(32) NOT NULL,
  `time_off` varchar(32) NOT NULL,
  `enable` tinyint(1) unsigned NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- 表的结构 `user_info`
--

CREATE TABLE IF NOT EXISTS `user_info` (
  `user_name` varchar(32) NOT NULL,
  `password` varchar(32) NOT NULL,
  `email` varchar(32) NOT NULL,
  `cookie` varchar(64) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- 表的结构 `user_module`
--

CREATE TABLE IF NOT EXISTS `user_module` (
  `user_name` varchar(32) NOT NULL,
  `module_id` varchar(32) NOT NULL,
  `ctrl_mode` tinyint(1) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `module_info`
--
ALTER TABLE `module_info`
 ADD PRIMARY KEY (`module_id`);

--
-- Indexes for table `timer_info`
--
ALTER TABLE `timer_info`
 ADD PRIMARY KEY (`timer_id`);

--
-- Indexes for table `user_info`
--
ALTER TABLE `user_info`
 ADD PRIMARY KEY (`user_name`);

--
-- Indexes for table `user_module`
--
ALTER TABLE `user_module`
 ADD PRIMARY KEY (`user_name`,`module_id`);

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;

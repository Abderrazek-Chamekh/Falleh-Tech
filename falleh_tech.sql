-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Apr 23, 2025 at 02:22 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.1.25

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `falleh_tech`
--

-- --------------------------------------------------------

--
-- Table structure for table `candidature`
--

CREATE TABLE `candidature` (
  `id` int(11) NOT NULL,
  `id_travailleur_id` int(11) NOT NULL,
  `id_offre_id` int(11) NOT NULL,
  `statut` varchar(255) NOT NULL,
  `date_applied` datetime NOT NULL COMMENT '(DC2Type:datetime_immutable)',
  `rating` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `categorie`
--

CREATE TABLE `categorie` (
  `id` int(11) NOT NULL,
  `nom` varchar(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `commande`
--

CREATE TABLE `commande` (
  `id` int(11) NOT NULL,
  `date_creation` datetime NOT NULL,
  `total` double NOT NULL,
  `status` varchar(255) DEFAULT NULL,
  `adresse_livraison` varchar(255) DEFAULT NULL,
  `mode_paiement` varchar(50) NOT NULL,
  `date_paiement` datetime DEFAULT NULL,
  `status_paiement` varchar(50) NOT NULL,
  `user_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `commande`
--

INSERT INTO `commande` (`id`, `date_creation`, `total`, `status`, `adresse_livraison`, `mode_paiement`, `date_paiement`, `status_paiement`, `user_id`) VALUES
(1, '2025-04-17 16:04:09', 58, 'Confirmée', 'olkjlklllll', 'Carte_Bancaire', NULL, 'Confirmée', 7);

-- --------------------------------------------------------

--
-- Table structure for table `comment`
--

CREATE TABLE `comment` (
  `id` int(11) NOT NULL,
  `post_id` int(11) DEFAULT NULL,
  `user_id` int(11) NOT NULL,
  `contenu` varchar(255) NOT NULL,
  `date` date NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `comment`
--

INSERT INTO `comment` (`id`, `post_id`, `user_id`, `contenu`, `date`) VALUES
(21, 12, 7, 'This helped me a lot, thanks.', '2025-04-17'),
(22, 13, 9, 'Perfect timing for the drought season.', '2025-04-17'),
(23, 13, 10, 'Useful content as always.', '2025-04-17'),
(24, 14, 12, 'Technology is changing agriculture!', '2025-04-17'),
(26, 15, 17, 'No more chemicals? I’m in.', '2025-04-17'),
(28, 12, 8, 'waaaa mamaaa', '2025-04-23');

-- --------------------------------------------------------

--
-- Table structure for table `doctrine_migration_versions`
--

CREATE TABLE `doctrine_migration_versions` (
  `version` varchar(191) NOT NULL,
  `executed_at` datetime DEFAULT NULL,
  `execution_time` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- Dumping data for table `doctrine_migration_versions`
--

INSERT INTO `doctrine_migration_versions` (`version`, `executed_at`, `execution_time`) VALUES
('DoctrineMigrations\\Version20250305192711', '2025-03-06 08:13:24', 812);

-- --------------------------------------------------------

--
-- Table structure for table `favoris`
--

CREATE TABLE `favoris` (
  `id` int(11) NOT NULL,
  `produit_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `created_at` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `like`
--

CREATE TABLE `like` (
  `id` int(11) NOT NULL,
  `post_id` int(11) DEFAULT NULL,
  `user_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `like`
--

INSERT INTO `like` (`id`, `post_id`, `user_id`) VALUES
(21, 12, 7),
(22, 12, 10),
(23, 12, 12),
(25, 13, 17),
(27, 14, 9),
(28, 15, 10),
(30, 16, 17),
(31, 14, 17),
(32, 12, 17);

-- --------------------------------------------------------

--
-- Table structure for table `livraison`
--

CREATE TABLE `livraison` (
  `id` int(11) NOT NULL,
  `commande_id` int(11) NOT NULL,
  `statut` varchar(50) NOT NULL,
  `transporteur` varchar(100) NOT NULL,
  `num_tel_transporteur` varchar(20) NOT NULL,
  `date_livraison` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `livraison`
--

INSERT INTO `livraison` (`id`, `commande_id`, `statut`, `transporteur`, `num_tel_transporteur`, `date_livraison`) VALUES
(2, 1, 'Livrée', 'qzqzdqzd', '14144141', '2025-04-24 00:00:00');

-- --------------------------------------------------------

--
-- Table structure for table `messenger_messages`
--

CREATE TABLE `messenger_messages` (
  `id` bigint(20) NOT NULL,
  `body` longtext NOT NULL,
  `headers` longtext NOT NULL,
  `queue_name` varchar(190) NOT NULL,
  `created_at` datetime NOT NULL COMMENT '(DC2Type:datetime_immutable)',
  `available_at` datetime NOT NULL COMMENT '(DC2Type:datetime_immutable)',
  `delivered_at` datetime DEFAULT NULL COMMENT '(DC2Type:datetime_immutable)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `notification`
--

CREATE TABLE `notification` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `post_id` int(11) NOT NULL,
  `message` longtext NOT NULL,
  `created_at` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `offre_emploi`
--

CREATE TABLE `offre_emploi` (
  `id` int(11) NOT NULL,
  `id_employeur_id` int(11) NOT NULL,
  `titre` varchar(100) NOT NULL,
  `description` longtext NOT NULL,
  `salaire` double NOT NULL,
  `lieu` varchar(255) NOT NULL,
  `start_date` date NOT NULL,
  `date_expiration` date NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `ouvrier_calendrier`
--

CREATE TABLE `ouvrier_calendrier` (
  `id` int(11) NOT NULL,
  `ouvrier_id` int(11) NOT NULL,
  `candidature_id` int(11) NOT NULL,
  `start_date` datetime NOT NULL,
  `end_date` datetime NOT NULL,
  `status` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `post`
--

CREATE TABLE `post` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `titre` varchar(255) NOT NULL,
  `contenu` varchar(255) NOT NULL,
  `date` date NOT NULL,
  `image` varchar(255) NOT NULL,
  `category` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `post`
--

INSERT INTO `post` (`id`, `user_id`, `titre`, `contenu`, `date`, `image`, `category`) VALUES
(12, 7, 'Benefits of organic alternatives for soil', 'Benefits of organic alternatives for soil', '2025-02-11', '467354271_1099823751861071_8112836696498361043_n.jpg', 'agriculture_news'),
(13, 8, 'Best irrigation methods for dry areas', 'Best irrigation methods for dry areas', '2025-04-17', 'GnqM8Dgb0AABkJP.jpeg', 'technology'),
(14, 9, 'Using IoT in agriculture', 'Using IoT in agriculture', '2025-06-18', '467354271_1099823751861071_8112836696498361043_n.jpg', 'technology'),
(15, 10, 'Healthy practices for pest control', 'Healthy practices for pest control', '2025-02-18', 'GnqM8Dgb0AABkJP.jpeg', 'recipes'),
(16, 17, 'statue of god', 'solo leveling', '2025-04-18', '480067610_122122942742619550_8226889747583201853_n.jpg', 'urban_farming'),
(17, 17, 'qzdqzdqzdqzdqzd', 'qzdqzdqzdqzdqzd', '2025-04-23', '1745362957103_download.gif', 'urban_farming');

-- --------------------------------------------------------

--
-- Table structure for table `produit`
--

CREATE TABLE `produit` (
  `id` int(11) NOT NULL,
  `categorie_id` int(11) NOT NULL,
  `sous_categorie_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `nom` varchar(100) NOT NULL,
  `prix` decimal(10,2) NOT NULL,
  `description` longtext NOT NULL,
  `image` varchar(255) DEFAULT NULL,
  `is_favorite` tinyint(1) NOT NULL,
  `stock` varchar(100) NOT NULL,
  `updated_at` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `reset_password_request`
--

CREATE TABLE `reset_password_request` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `selector` varchar(20) NOT NULL,
  `hashed_token` varchar(100) NOT NULL,
  `requested_at` datetime NOT NULL COMMENT '(DC2Type:datetime_immutable)',
  `expires_at` datetime NOT NULL COMMENT '(DC2Type:datetime_immutable)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `sous_categorie`
--

CREATE TABLE `sous_categorie` (
  `id` int(11) NOT NULL,
  `categorie_id` int(11) NOT NULL,
  `nom` varchar(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `user`
--

CREATE TABLE `user` (
  `id` int(11) NOT NULL,
  `name` varchar(20) NOT NULL,
  `last_name` varchar(20) NOT NULL,
  `email` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `phone_number` varchar(20) NOT NULL,
  `role` varchar(20) NOT NULL,
  `carte_identite` varchar(8) NOT NULL,
  `disponibility` datetime DEFAULT NULL,
  `location` varchar(255) DEFAULT NULL,
  `experience` varchar(255) DEFAULT NULL,
  `active` tinyint(1) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `user`
--

INSERT INTO `user` (`id`, `name`, `last_name`, `email`, `password`, `phone_number`, `role`, `carte_identite`, `disponibility`, `location`, `experience`, `active`) VALUES
(1, 'abidi', 'ghof', 'ghofraneabidii@gmail.com', '$2y$13$wTCKFhfEJbltkoGZVFM07O/oCEI2B3OsgrYHvqg55biz3LuQryz9G', '+21693037238', 'agriculteur', '12345678', NULL, NULL, NULL, 1),
(2, 'ghofraneabid', 'ss', 'ghofraneabidiii@gmail.com', '$2y$13$EHdpN47GaBfZAbA8VzElde44TxXkqYS.Hrf8rJm8oywMYeb5vyHtu', '+21693037238', 'ROLE_ADMIN', '12345678', NULL, NULL, NULL, 0),
(7, 'Mohamed', 'Ben Ali', 'mohamed.ali@example.com', 'hashed_password_1', '12345678', 'Agriculteur', '87654321', NULL, NULL, NULL, 1),
(8, 'Fatima', 'Zahra', 'fatima@gmail.com', '$2a$13$jz89t9nIMATSeuw0lKGQleT4ENGctAJD8wy7cxfkPc7BX9YvwtoXW', '23456789', 'Client', '98765432', NULL, NULL, NULL, 1),
(9, 'Karim', 'Bouazizi', 'karim@gmail.com', '$2a$13$QlRXjUP4/Da5J9hdad13M.HwOOclIs7lP9ri1yQfG8Cno73jgKK86', '34567890', 'Ouvrier', '11223344', '2023-12-15 08:00:00', 'Tunis', '5 years in agriculture', 0),
(10, 'Amina', 'Trabelsi', 'amina.t@example.com', 'hashed_password_4', '45678901', 'Agriculteur', '22334455', NULL, NULL, NULL, 1),
(12, 'Leila', 'Gharbi', 'leila.g@example.com', 'hashed_password_6', '67890123', 'Ouvrier', '44556677', '2023-12-20 00:00:00', 'Sousse', '3 years in harvesting', 1),
(17, 'mohamed', 'ali', 'mohamed@gmail.com', '$2a$13$jz89t9nIMATSeuw0lKGQlea2ddswh3OLlluxKuqYygdpQGVKRnDC.', '26156295', 'ROLE_ADMIN', '62956284', NULL, NULL, NULL, 1),
(20, 'Admin', 'System', 'admin@example.com', '$2y$13$wTCKFhfEJbltkoGZVFM07O/oCEI2B3OsgrYHvqg55bi', '+21612345678', 'ROLE_ADMIN', '11223344', NULL, NULL, NULL, 1),
(21, 'Samir', 'Ben Ammar', 'samir.agriculteur@example.com', '$2a$13$jz89t9nIMATSeuw0lKGQleT4ENGctAJD8wy7cxfkPc7', '+21623456789', 'Agriculteur', '33445566', NULL, 'Sfax', '10 years experience', 1),
(22, 'Salma', 'Ben Youssef', 'salma.client@example.com', '$2a$13$jz89t9nIMATSeuw0lKGQleluFxm9bGEu.zg7Krwea2G', '+21634567890', 'Client', '55667788', NULL, 'Tunis', NULL, 1),
(23, 'Hakim', 'Mansouri', 'hakim.ouvrier@example.com', '$2a$13$jz89t9nIMATSeuw0lKGQlea2ddswh3OLlluxKuqYygd', '+21645678901', 'Ouvrier', '77889900', '2023-12-25 08:00:00', 'Nabeul', '7 years in farming', 1),
(24, 'Nadia', 'Chaabane', 'nadia.agriculteur@example.com', '$2a$13$jz89t9nIMATSeuw0lKGQleT4ENGctAJD8wy7cxfkPc7', '+21656789012', 'Agriculteur', '99001122', NULL, 'Bizerte', '8 years experience', 0);

--
-- Indexes for dumped tables
--

--
-- Indexes for table `candidature`
--
ALTER TABLE `candidature`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_E33BD3B8545CC730` (`id_travailleur_id`),
  ADD KEY `IDX_E33BD3B81C13BCCF` (`id_offre_id`);

--
-- Indexes for table `categorie`
--
ALTER TABLE `categorie`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `commande`
--
ALTER TABLE `commande`
  ADD PRIMARY KEY (`id`),
  ADD KEY `RE` (`user_id`);

--
-- Indexes for table `comment`
--
ALTER TABLE `comment`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_9474526C4B89032C` (`post_id`),
  ADD KEY `IDX_9474526CA76ED395` (`user_id`);

--
-- Indexes for table `doctrine_migration_versions`
--
ALTER TABLE `doctrine_migration_versions`
  ADD PRIMARY KEY (`version`);

--
-- Indexes for table `favoris`
--
ALTER TABLE `favoris`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_8933C432F347EFB` (`produit_id`),
  ADD KEY `IDX_8933C432A76ED395` (`user_id`);

--
-- Indexes for table `like`
--
ALTER TABLE `like`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_AC6340B34B89032C` (`post_id`),
  ADD KEY `IDX_AC6340B3A76ED395` (`user_id`);

--
-- Indexes for table `livraison`
--
ALTER TABLE `livraison`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UNIQ_A60C9F1F82EA2E54` (`commande_id`);

--
-- Indexes for table `messenger_messages`
--
ALTER TABLE `messenger_messages`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_75EA56E0FB7336F0` (`queue_name`),
  ADD KEY `IDX_75EA56E0E3BD61CE` (`available_at`),
  ADD KEY `IDX_75EA56E016BA31DB` (`delivered_at`);

--
-- Indexes for table `notification`
--
ALTER TABLE `notification`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_BF5476CAA76ED395` (`user_id`),
  ADD KEY `IDX_BF5476CA4B89032C` (`post_id`);

--
-- Indexes for table `offre_emploi`
--
ALTER TABLE `offre_emploi`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_132AD0D17EBD269E` (`id_employeur_id`);

--
-- Indexes for table `ouvrier_calendrier`
--
ALTER TABLE `ouvrier_calendrier`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UNIQ_87074B28B6121583` (`candidature_id`),
  ADD KEY `IDX_87074B284E853A9E` (`ouvrier_id`);

--
-- Indexes for table `post`
--
ALTER TABLE `post`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_5A8A6C8DA76ED395` (`user_id`);

--
-- Indexes for table `produit`
--
ALTER TABLE `produit`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_29A5EC27BCF5E72D` (`categorie_id`),
  ADD KEY `IDX_29A5EC27365BF48` (`sous_categorie_id`),
  ADD KEY `IDX_29A5EC27A76ED395` (`user_id`);

--
-- Indexes for table `reset_password_request`
--
ALTER TABLE `reset_password_request`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_7CE748AA76ED395` (`user_id`);

--
-- Indexes for table `sous_categorie`
--
ALTER TABLE `sous_categorie`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_52743D7BBCF5E72D` (`categorie_id`);

--
-- Indexes for table `user`
--
ALTER TABLE `user`
  ADD PRIMARY KEY (`id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `candidature`
--
ALTER TABLE `candidature`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `categorie`
--
ALTER TABLE `categorie`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `commande`
--
ALTER TABLE `commande`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `comment`
--
ALTER TABLE `comment`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=29;

--
-- AUTO_INCREMENT for table `favoris`
--
ALTER TABLE `favoris`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `like`
--
ALTER TABLE `like`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=33;

--
-- AUTO_INCREMENT for table `livraison`
--
ALTER TABLE `livraison`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `messenger_messages`
--
ALTER TABLE `messenger_messages`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `notification`
--
ALTER TABLE `notification`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT for table `offre_emploi`
--
ALTER TABLE `offre_emploi`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `ouvrier_calendrier`
--
ALTER TABLE `ouvrier_calendrier`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `post`
--
ALTER TABLE `post`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=18;

--
-- AUTO_INCREMENT for table `produit`
--
ALTER TABLE `produit`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `reset_password_request`
--
ALTER TABLE `reset_password_request`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `sous_categorie`
--
ALTER TABLE `sous_categorie`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `user`
--
ALTER TABLE `user`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=25;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `candidature`
--
ALTER TABLE `candidature`
  ADD CONSTRAINT `FK_E33BD3B81C13BCCF` FOREIGN KEY (`id_offre_id`) REFERENCES `offre_emploi` (`id`),
  ADD CONSTRAINT `FK_E33BD3B8545CC730` FOREIGN KEY (`id_travailleur_id`) REFERENCES `user` (`id`);

--
-- Constraints for table `commande`
--
ALTER TABLE `commande`
  ADD CONSTRAINT `RE` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `comment`
--
ALTER TABLE `comment`
  ADD CONSTRAINT `FK_9474526C4B89032C` FOREIGN KEY (`post_id`) REFERENCES `post` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `FK_9474526CA76ED395` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `favoris`
--
ALTER TABLE `favoris`
  ADD CONSTRAINT `FK_8933C432A76ED395` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `FK_8933C432F347EFB` FOREIGN KEY (`produit_id`) REFERENCES `produit` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `like`
--
ALTER TABLE `like`
  ADD CONSTRAINT `FK_AC6340B34B89032C` FOREIGN KEY (`post_id`) REFERENCES `post` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `FK_AC6340B3A76ED395` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `livraison`
--
ALTER TABLE `livraison`
  ADD CONSTRAINT `FK_A60C9F1F82EA2E54` FOREIGN KEY (`commande_id`) REFERENCES `commande` (`id`);

--
-- Constraints for table `notification`
--
ALTER TABLE `notification`
  ADD CONSTRAINT `FK_BF5476CA4B89032C` FOREIGN KEY (`post_id`) REFERENCES `post` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `FK_BF5476CAA76ED395` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);

--
-- Constraints for table `offre_emploi`
--
ALTER TABLE `offre_emploi`
  ADD CONSTRAINT `FK_132AD0D17EBD269E` FOREIGN KEY (`id_employeur_id`) REFERENCES `user` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `ouvrier_calendrier`
--
ALTER TABLE `ouvrier_calendrier`
  ADD CONSTRAINT `FK_87074B284E853A9E` FOREIGN KEY (`ouvrier_id`) REFERENCES `user` (`id`),
  ADD CONSTRAINT `FK_87074B28B6121583` FOREIGN KEY (`candidature_id`) REFERENCES `candidature` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `post`
--
ALTER TABLE `post`
  ADD CONSTRAINT `FK_5A8A6C8DA76ED395` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `produit`
--
ALTER TABLE `produit`
  ADD CONSTRAINT `FK_29A5EC27365BF48` FOREIGN KEY (`sous_categorie_id`) REFERENCES `sous_categorie` (`id`),
  ADD CONSTRAINT `FK_29A5EC27A76ED395` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  ADD CONSTRAINT `FK_29A5EC27BCF5E72D` FOREIGN KEY (`categorie_id`) REFERENCES `categorie` (`id`);

--
-- Constraints for table `reset_password_request`
--
ALTER TABLE `reset_password_request`
  ADD CONSTRAINT `FK_7CE748AA76ED395` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);

--
-- Constraints for table `sous_categorie`
--
ALTER TABLE `sous_categorie`
  ADD CONSTRAINT `FK_52743D7BBCF5E72D` FOREIGN KEY (`categorie_id`) REFERENCES `categorie` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;

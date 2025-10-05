library(ggplot2)
library(tidyverse)

distribution <- 
    read.csv(file = "./vsum/results_echange.csv")

ggplot(distribution, aes(EChangeType, Time)) +
  geom_boxplot(outliers = FALSE)

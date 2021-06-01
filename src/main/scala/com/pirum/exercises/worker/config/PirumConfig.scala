package com.pirum.exercises.worker.config

import com.typesafe.config.{Config, ConfigFactory}

trait PirumConfig {
  def config: Config = ConfigFactory.load()
}

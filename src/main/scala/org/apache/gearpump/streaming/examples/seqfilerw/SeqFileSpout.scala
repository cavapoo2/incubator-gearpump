/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.gearpump.streaming.examples.seqfilerw

import org.apache.gearpump.streaming.task.{Message, TaskActor}
import org.apache.gearpump.util.Configs
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{Path, FileSystem}
import org.apache.hadoop.io.SequenceFile
import org.apache.hadoop.io.SequenceFile._
import org.apache.hadoop.io.Text
import org.apache.gearpump.streaming.examples.seqfilerw.SeqFileSpout._
import org.slf4j.{LoggerFactory, Logger}

class SeqFileSpout(config: Configs) extends TaskActor(config ){
  private val LOG: Logger = LoggerFactory.getLogger(classOf[SeqFileSpout])
  val value = new Text()
  val key = new Text()
  var reader: SequenceFile.Reader = null
  val hadoopConf = new Configuration()
  val fs = FileSystem.get(hadoopConf)
  val inputPath = new Path(config.getString(INPUT_PATH))

  override def onStart() = {
    reader = new SequenceFile.Reader(hadoopConf, Reader.file(inputPath))
    self ! Start
    LOG.info("sequence file spout initiated")
  }

  override def onNext(msg: String) = {
    if(reader.next(key, value)){
      output(key + "++" + value)
    } else {
      reader.close()
      reader = new SequenceFile.Reader(hadoopConf, Reader.file(inputPath))
    }
    self ! Continue
  }

  override def onStop(): Unit ={
    reader.close()
  }
}

object SeqFileSpout{
  def INPUT_PATH = "inputpath"

  val Start = Message(0, "start")
  val Continue = Message(0, "continue")
}

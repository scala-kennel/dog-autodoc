/*
The MIT License (MIT)

Copyright (c) 2015 Ken Kaizu

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
package dog
package twirl

// copy from https://github.com/krrrr38/play-autodoc
import play.twirl.api.{ BufferedContent, Format }

import scala.collection.immutable
import play.twirl.api.Formats

/**
 * Type used in default Makrodown templates.
 */
class Markdown private (elements: immutable.Seq[Markdown], text: String) extends BufferedContent[Markdown](elements, text) {
  def this(text: String) = this(Nil, Formats.safe(text))
  def this(elements: immutable.Seq[Markdown]) = this(elements, "")

  /**
   * Content type of Markdown
   */
  val contentType = "text/x-markdown"
}

/**
 * Helper for Markdown utility methods.
 */
object Markdown {

  /**
   * Creates an Markdown fragment with initial content specified.
   */
  def apply(text: String): Markdown = {
    new Markdown(text)
  }
}

/**
 * Formatter for Markdown content.
 * which is same as TxtFormat
 */
object MarkdownFormat extends Format[Markdown] {

  /**
   * Create a Markdown fragment.
   */
  def raw(text: String) = Markdown(text)

  /**
   * No need for a safe (escaped) text fragment.
   */
  def escape(text: String) = Markdown(text)

  /**
   * Generate an empty Markdown fragment
   */
  val empty: Markdown = new Markdown("")

  /**
   * Create an Markdown Fragment that holds other fragments.
   */
  def fill(elements: immutable.Seq[Markdown]): Markdown = new Markdown(elements)

}

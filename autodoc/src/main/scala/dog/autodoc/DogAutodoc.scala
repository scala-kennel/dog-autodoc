package dog
package autodoc

trait DogAutodoc extends Dog {
  def markdown: Autodoc.Format = Autodoc.Markdown()
  def html: Autodoc.Format = Autodoc.Html()
}

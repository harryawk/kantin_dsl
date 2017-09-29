import canteen.dsl.Canteen

import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer

import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import groovy.util.logging.Log

def configuration = new CompilerConfiguration()

def imports = new ImportCustomizer()
// imports.addStaticStars(mars.Direction.name)
imports.addImports("canteen.dsl.Canteen")
imports.addImports("canteen.dsl.Food")
imports.addImports("canteen.dsl.FoodIngredient")
imports.addImports("canteen.dsl.MenuOrderer")
imports.addImports("canteen.dsl.Order")
imports.addImports("canteen.dsl.StockBuyer")
imports.addImports("canteen.dsl.Transaction")
imports.addImports("canteen.dsl.TransactionItem")
configuration.addCompilationCustomizers(imports, 
            new ASTTransformationCustomizer(Log))

def binding = new Binding([
    process: Canteen.&process
])

def shell = new GroovyShell(binding, configuration)
def filename = this.args ? this.args[0] : 'client.groovy'
println filename
shell.evaluate(
    new File(filename)
)
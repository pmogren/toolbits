
class Department{
	
	public String name = "default"
	public def Employee = []
	
	public String print(){
		println "Department: " + name
		this.Employee.each{it.print()}
	}
}

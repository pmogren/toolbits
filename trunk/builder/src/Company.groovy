
class Company{
	
	public def deptList = []
	public def name = ""
	
	public add(Department d){
		deptList.add(d)
	}
	
	public String print(){
		println "Company: ${name}"
		deptList.each{it.print()}
	}
}

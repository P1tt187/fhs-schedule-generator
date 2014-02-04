package models.fhs.pages.roomdefinition

/**
 * Created by fabian on 04.02.14.
 */
case class MRoomdefintion(capacity: Int, house: String, number: Int, pcpools: Boolean, beamer: Boolean, timeCriterias: List[MTtimeslotCritDefine])

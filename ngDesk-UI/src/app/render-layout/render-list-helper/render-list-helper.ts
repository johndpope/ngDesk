import { Injectable } from '@angular/core';
import { RenderListLayoutService } from '../render-list-layout-new/render-list-layout.service';
import { UsersService } from '@src/app/users/users.service';

@Injectable()
export class RenderListHelper{

    constructor(
        private renderListLayoutService:RenderListLayoutService,
        private usersService:UsersService
    ){

    }

    public getSearchQuey(moduleId){
       return this.renderListLayoutService.getSearchQuey(moduleId)
    }

    public defaultListLayout(module){
        return module.LIST_LAYOUTS.find(
            (layout) =>
                layout.ROLE === this.usersService.user.ROLE && layout.IS_DEFAULT
        );
    }

    public getSearchString(args){
        return null
    }
}
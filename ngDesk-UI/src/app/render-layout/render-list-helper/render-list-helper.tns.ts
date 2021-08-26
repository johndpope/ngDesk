import { Injectable } from '@angular/core';
import { UsersService } from '@src/app/users/users.service';
import { SearchBar } from '@nativescript/core';
// import { MatDialog } from '@angular/material/dialog';
@Injectable()
export class RenderListHelper{

    constructor(
        // private dialog:MatDialog,
        private usersService:UsersService
    ){
        
    }

    public getSearchQuey(args){
       return null;
    }

    public defaultListLayout(module){
        const defaultListLayout = module.LIST_MOBILE_LAYOUTS.find(
            (layout) =>
                layout.ROLE === this.usersService.user.ROLE && layout.IS_DEFAULT
        );
        if(defaultListLayout){
            return defaultListLayout;
        }else{
           const listLayout = module.LIST_MOBILE_LAYOUTS.find(
                (layout) =>
                    layout.ROLE === this.usersService.user.ROLE
            );
            console.log(listLayout)
            if(!listLayout){
                console.log(listLayout)
                return false;
            }
            return listLayout;
        }
      
    }

    public getSearchString(args){
        let searchText:any;
        const searchBar = args.object as SearchBar;
        console.log(`Searching for ${searchBar.text}`);
        searchText = searchBar.text;
        return searchText;
    }

}